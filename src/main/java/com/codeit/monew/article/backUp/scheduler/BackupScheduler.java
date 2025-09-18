package com.codeit.monew.article.backUp.scheduler;

import com.codeit.monew.article.backUp.service.ArticleBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;


@Component
@RequiredArgsConstructor
@Slf4j
public class BackupScheduler {
    private final ArticleBackupService backupService;

    // 초, 분, 시, 일, 월, 요일
    // 매일 0시, 전날의 데이터를 백업함
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void backupArticle() {
        LocalDate target = LocalDate.now(ZoneId.of("Asia/Seoul"));
//        .minusDays(1)
        log.info("뉴스기사 백업 시작, {}", target);
        backupService.articleBackup(target);
    }
}
