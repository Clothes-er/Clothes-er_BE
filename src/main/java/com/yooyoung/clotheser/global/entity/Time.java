package com.yooyoung.clotheser.global.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.yooyoung.clotheser.global.entity.Time.TIME_MAXIMUM.*;

public class Time {

    static class TIME_MAXIMUM {
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }

    public static String calculateTime(LocalDateTime localDateTime) {
        LocalDateTime now = LocalDateTime.now();

        // now보다 이후면 +, 전이면 -
        long diffTime = localDateTime.until(now, ChronoUnit.SECONDS);
        System.out.println("시간 차이: " + diffTime);

        String msg = null;
        if (diffTime < SEC){
            msg = diffTime + "초 전";
        }
        else if ((diffTime /= SEC) < MIN) {
            msg = diffTime + "분 전";
        }
        else if ((diffTime /= MIN) < HOUR) {
            msg = diffTime + "시간 전";
        }
        else if ((diffTime /= HOUR) < DAY) {
            msg = diffTime + "일 전";
        }
        else if ((diffTime /= DAY) < MONTH) {
            msg = diffTime + "개월 전";
        }
        else {
            diffTime = diffTime / MONTH;
            msg = diffTime + "년 전";
        }

        return msg;
    }

}
