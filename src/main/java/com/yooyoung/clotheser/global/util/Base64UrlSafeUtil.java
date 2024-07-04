package com.yooyoung.clotheser.global.util;

import java.util.Base64;

public class Base64UrlSafeUtil {

    // +, /, = -> -, _, .으로 대체
    public static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes());
    }

    public static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value));
    }
}
