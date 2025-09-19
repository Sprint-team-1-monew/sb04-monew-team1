package com.codeit.monew.article.backUp.aws;

import java.time.LocalDate;

public class BackupKeyMaker {
    // S3에 백업파일을 저장할 때 파일 경로(키)를 만들어주는 유틸리티
    private BackupKeyMaker() {}

    public static String keyFor(LocalDate date) {
        String y = "%04d".formatted(date.getYear());
        String m = "%02d".formatted(date.getMonthValue());
        String d = "%02d".formatted(date.getDayOfMonth());
        return "backups/articles/%s/%s/%s/articles-%s-%s-%s.jsonl.gz"
                .formatted(y,m,d,y,m,d); // backups/articles/2025/09/16/articles-2025-09-16.jsonl.gz
    }

    // 위 파일명을 임시 업로드용 파일명으로 바꾸는 메서드
    // 임시 키(.part)로 먼저 업로드하고, 업로드 성공하면 최종 키(.json.gz)로 복사 후 임시파일 삭제
    public static String tempKeyFor(LocalDate date, String uuid) {
        return keyFor(date).replace(".jsonl.gz", "." + uuid + ".part");
    }
}