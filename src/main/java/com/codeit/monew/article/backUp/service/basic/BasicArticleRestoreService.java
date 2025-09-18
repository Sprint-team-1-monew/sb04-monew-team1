package com.codeit.monew.article.backUp.service.basic;

import com.codeit.monew.article.backUp.aws.BackupKeyMaker;
import com.codeit.monew.article.backUp.dto.ArticleBackupDto;
import com.codeit.monew.article.backUp.dto.ArticleRestoreResultDto;
import com.codeit.monew.article.backUp.service.ArticleRestoreService;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.repository.InterestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.GZIPInputStream;


@Service
@RequiredArgsConstructor
@Slf4j
public class BasicArticleRestoreService implements ArticleRestoreService {

    private static final int BATCH_SIZE = 1000;

    private final ArticleRepository articleRepository;
    private final InterestRepository interestRepository;
    private final S3Client s3;

    @Value("${app.backup.bucket}")
    String bucket;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // from, to가 들어오면 날짜로 바꿔서 일단위로 복구하기(백업을 일단위로 했기 때문에)
    @Transactional
    @Override
    public ArticleRestoreResultDto restoreArticle(String fromStr, String toStr) {
        LocalDate from = parseToLocalDate(fromStr); // 시작점
        LocalDate to = parseToLocalDate(toStr); // 끝

        // 끝이 시작점보다 크면 예외 발생
        if(to.isBefore(from)) {
            throw new IllegalArgumentException("값이 잘못되었습니다. to는 from보다 커야합니다.");
        }

        long totalInserted = 0;
        List<String> details = new ArrayList<>();

        for(LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            long n = restoreOneDate(d);
            totalInserted += n;
            details.add(d + ": inserted=" + n);
        }

//        // 중복검사를 하고 없는 뉴스기사를 복구하기
//        isArticleDuplicated()

        return null;
    }


    /**
     * 아래는 도우미 메서드
     */

    private boolean isArticleDuplicated(Article article) {
        Optional<Article> articleOptional = articleRepository.findBySourceUrl(article.getSourceUrl()); // 없어야 됨
        if (articleOptional.isPresent()) {
            return true;
        }
        return false;
    }

    private long flushBatch(List<Article> batch) {
        long n = articleRepository.saveAll(batch).size();
        batch.clear();
        return n;
    }

    private Interest resolveInterest(String interestId) { // interest에서 UUID인 id를 찾고 String으로 반환해줌
        if(interestId == null || interestId.isBlank()) {
            return null;
        }

        try {
            return interestRepository.findById(UUID.fromString(interestId)).orElse(null);
        } catch(IllegalArgumentException e) {
            return null;
        }
    }

    @Transactional
    public long restoreOneDate(LocalDate date) {
        String key = BackupKeyMaker.keyFor(date);
        long inserted = 0;

        try(var s3is = s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build()); // 키를 가져옴
            var gis = new GZIPInputStream(s3is); // 스트림
            var br = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8)) // 버퍼
                ) {
            List<Article> batch = new ArrayList<>(BATCH_SIZE);
            String line;

            while ((line = br.readLine()) != null) {
                ArticleBackupDto dto = objectMapper.readValue(line,ArticleBackupDto.class);

                if(dto.sourceUrl() == null || dto.sourceUrl().isBlank()) { continue; }
                if(articleRepository.existsBySourceUrl(dto.sourceUrl())) { continue; }

                Interest interest = resolveInterest(dto.interestId());
                if(interest == null) { continue; }

                // 엔티티 채워 넣기
                Article entity = Article.builder()
                        .source(dto.source())
                        .sourceUrl(dto.sourceUrl())
                        .articleTitle(dto.articleTitle())
                        .articlePublishDate(dto.articlePublishDate())
                        .articleSummary(dto.articleSummary())
                        .articleCommentCount(dto.articleCommentCount())
                        .articleViewCount(dto.articleViewCount())
                        .deleted(dto.deleted())
                        .interest(interest)
                        .build();

                batch.add(entity);

                if (batch.size() >= BATCH_SIZE) {
                    inserted += flushBatch(batch);
                }
            }

            if (!batch.isEmpty()) {
                inserted += flushBatch(batch);
            }

            log.info("Restore {}: inserted={}", date, inserted);
            return inserted;

        } catch (NoSuchKeyException e) {
            log.warn("No backup file for {} at s3://{}/{}", date, bucket, key);
            return 0;
        } catch (Exception e) {
            log.error("Restore {} failed", date, e);
            throw new RuntimeException(e);
        }
    }

    private static LocalDate parseToLocalDate(String dateStr) {
        ZoneId KST = ZoneId.of("Asia/Seoul");
        try {
            return OffsetDateTime.parse(dateStr).atZoneSameInstant(KST).toLocalDate();
        } catch (Exception e) {}
        try {
            return LocalDateTime.parse(dateStr).atZone(KST).toLocalDate();
        } catch(Exception e) {}
        try {
            return LocalDate.parse(dateStr);
        } catch(Exception e) {}
        throw new IllegalArgumentException("DateTime 찾을 수 없음" + dateStr);
    }
}
