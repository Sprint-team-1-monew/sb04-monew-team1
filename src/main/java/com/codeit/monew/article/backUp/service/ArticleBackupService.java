package com.codeit.monew.article.backUp.service;

import com.codeit.monew.article.backUp.dto.ArticleBackupDto;
import com.codeit.monew.article.backUp.aws.BackupKeyMaker;
import com.codeit.monew.article.backUp.repository.ArticleBackupRepository;
import com.codeit.monew.article.entity.Article;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleBackupService {

    private static final int PAGE_SIZE = 1000;

    private final ArticleBackupRepository articleBackupRepository;
    private final S3Client s3;

    @Value("${app.backup.bucket}")
    String bucket;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    // 날짜에 따라 백업하는 메서드
    public void articleBackup(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        String backupKey = BackupKeyMaker.keyFor(date);
        String tempKey = BackupKeyMaker.tempKeyFor(date, UUID.randomUUID().toString()); // UUID String

        Path tmp = null;
        try {
            tmp = Files.createTempFile("articleBackup",".jsonl.gz");
            System.out.println("tmp 경로 : " + tmp.toAbsolutePath());
            try (
                    OutputStream outOs = Files.newOutputStream(tmp, StandardOpenOption.WRITE);
                    GZIPOutputStream gzipOs = new GZIPOutputStream(outOs);
                    OutputStreamWriter outOsWriter = new OutputStreamWriter(gzipOs, StandardCharsets.UTF_8)
            )
            {
                Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("id").ascending()); // import 주의
                Page<Article> page;
                long total = 0;

                do {
                    page = articleBackupRepository.findAllInRange(start, end, pageable);
                    for(Article article : page.getContent()) {
                        ArticleBackupDto articleDto = ArticleBackupDto.from(article);
                        outOsWriter.write(objectMapper.writeValueAsString(articleDto));
                        outOsWriter.write('\n');
                        total++;
                    }
                    outOsWriter.flush();
                    pageable = page.nextPageable();
                } while(!page.isLast());

                log.info("백업 작성 종료됨, date={}, count={}, file={}", date, total, tmp);

            } // 안쪽 try

            // S3 임시 키 업로드하기
            long size = Files.size(tmp);
            PutObjectRequest putObject = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(tempKey)
                    .contentType("application/x-ndjson")
                    .contentEncoding("gzip")
                    .build();
            try(InputStream input = Files.newInputStream(tmp)) { // s3에 올리는 로직
                s3.putObject(putObject, RequestBody.fromInputStream(input,size));
            }

            s3.copyObject(c -> c
                    .sourceBucket(bucket).sourceKey(tempKey)
                    .destinationBucket(bucket).destinationKey(backupKey));
            s3.deleteObject(b -> b.bucket(bucket).key(tempKey));

            log.info("백업 업로드됨, s3://{}/{}", bucket, backupKey);

        } catch(Exception e) {
          log.error("백업 실패함, date = {}", date, e);
          throw new RuntimeException(e);
        } finally {
            if(tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException ioException) {

                }
            }
        } // 바깥쪽 try
    }

}