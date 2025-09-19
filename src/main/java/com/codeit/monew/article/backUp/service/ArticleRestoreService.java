package com.codeit.monew.article.backUp.service;

import com.codeit.monew.article.backUp.aws.BackupKeyMaker;
import com.codeit.monew.article.backUp.dto.ArticleBackupDto;
import com.codeit.monew.article.backUp.dto.ArticleRestoreResultDto;
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
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
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
public class ArticleRestoreService {
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
    public List<ArticleRestoreResultDto> restoreArticle(String fromStr, String toStr) {
        LocalDate from = parseToLocalDate(fromStr); // 시작점
        LocalDate to = parseToLocalDate(toStr); // 끝
        List<ArticleRestoreResultDto> results = new ArrayList<>();

        // 끝이 시작점보다 크면 예외 발생
        if(to.isBefore(from)) {
            throw new IllegalArgumentException("값이 잘못되었습니다. to는 from보다 커야합니다.");
        }

        long totalInserted = 0;
        List<String> details = new ArrayList<>();

        for(LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            List<Article> targetArticles = restoreOneDate(d);
            List<UUID> restoredIds = new ArrayList<>();

            for(Article article : targetArticles) {
                Optional<Article> checkArticle = articleRepository.findById(article.getId());
                // Article 중복검사, Deleted가 false인 애들만 필터링

                if(checkArticle.isPresent()) { // article이 있을 때
                    if (article.isDeleted()) { // true이면, 지워진거니까 deleted를 false로 바꾸기
                        checkArticle.get().setDeleted(false);
                        articleRepository.save(checkArticle.get());
                        restoredIds.add(article.getId());
                    }
                } else { // checkArticle에서 Article이 없을 때, checkArticle.isPresent() = false이면 (hard Delete)
                    Article newArticle = Article.builder()
                            .source(article.getSource())
                            .sourceUrl(article.getSourceUrl())
                            .articleTitle(article.getArticleTitle())
                            .articlePublishDate(article.getArticlePublishDate())
                            .articleSummary(article.getArticleSummary())
                            .articleCommentCount(0)
                            .articleViewCount(0)
                            .deleted(false)
                            .interest(article.getInterest())
                            .build();
//                    article.setDeleted(false);
                    articleRepository.save(newArticle); // 이러면 null값을 추가하는것이다
                    restoredIds.add(newArticle.getId());
                }
            }
            results.add(new ArticleRestoreResultDto(
                    LocalDateTime.now(),
                    restoredIds,
                    (long) restoredIds.size()
            ));
        }
        return results;
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

    @Transactional
    public List<Article> restoreOneDate(LocalDate date) {
        String key = BackupKeyMaker.keyFor(date);
//        long inserted = 0;
        List<Article> result = new ArrayList<>();

        try(var s3is = s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build()); // 키를 가져옴
            var gis = new GZIPInputStream(s3is); // 스트림
            var br = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8)) // 버퍼
                ) {
//            List<Article> batch = new ArrayList<>(BATCH_SIZE);
            String line;

            HeadObjectResponse head = s3.headObject(b -> b.bucket(bucket).key(key));
            log.info("S3 object size={} bytes, key={}", head.contentLength(), key);

            log.info("br 메세지를 찾기 위한 더미 텍스트 : {}", br.readLine());
            while ((line = br.readLine()) != null) {
                ArticleBackupDto dto = objectMapper.readValue(line,ArticleBackupDto.class);
                log.info("dtoURL: {}", dto.sourceUrl());

                Optional<Interest> interest = interestRepository.findById(dto.interestId());
                if(interest.isEmpty()) { // 관심사가 이미 지워진 상태일 땐, 복구하지 않는다
                    continue;
                }
                // 엔티티 채워 넣기
                Article entity = Article.builder()
                        .id(dto.id())
                        .createdAt(dto.createdAt())
                        .source(dto.source())
                        .sourceUrl(dto.sourceUrl())
                        .articleTitle(dto.articleTitle())
                        .articlePublishDate(dto.articlePublishDate())
                        .articleSummary(dto.articleSummary())
                        .articleCommentCount(dto.articleCommentCount())
                        .articleViewCount(dto.articleViewCount())
                        .deleted(dto.deleted())
                        .interest(interest.get())
                        .build();

                result.add(entity);
            }
            log.info("Restore {}: inserted={}", date, result);
            return result;

        } catch (NoSuchKeyException e) {
            log.warn("No backup file for {} at s3://{}/{}", date, bucket, key);
            return result;
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
