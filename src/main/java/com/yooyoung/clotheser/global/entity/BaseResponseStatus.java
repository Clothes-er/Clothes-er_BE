package com.yooyoung.clotheser.global.entity;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

    /*
        1000 : 요청 성공
    */
    SUCCESS(true, 1000, "요청에 성공하였습니다."),


    /*
        2000 : Request 오류
    */
    // 1. Common (2000 ~ 2099)
    REQUEST_ERROR(false, 2000, "입력값을 확인해주세요."),
    EMPTY_JWT(false, 2001, "JWT를 입력해주세요."),
    INVALID_USER_JWT(false, 2002, "권한이 없는 유저의 접근입니다."),
    INVALID_JWT_TOKEN(false, 2003, "유효하지 않은 JWT입니다."),
    EXPIRED_JWT_TOKEN(false, 2004, "만료된 JWT입니다."),
    UNSUPPORTED_JWT_TOKEN(false, 2005, "지원하지 않는 JWT입니다."),
    EMPTY_JWT_CLAIMS(false, 2006, "JWT가 잘못되었습니다."),
    REQUEST_FIRST_LOGIN(false, 2007, "최초 로그인이 필요합니다."),

    // 2. User (2100 ~ 2199)
    NOT_FOUND_USER(false, 2100, "회원을 찾을 수 없습니다."),

    // 회원가입
    PASSWORD_CONFIRMATION_MISMATCH(false, 2200, "비밀번호가 일치하지 않습니다."),
    NICKNAME_EXISTS(false, 2201, "중복된 닉네임입니다."),
    EMAIL_EXISTS(false, 2202, "중복된 이메일입니다."),
    PHONE_NUMBER_EXISTS(false, 2203, "중복된 전화번호입니다."),

    // 최초 로그인
    IS_NOT_FIRST_LOGIN(false, 2300, "최초 로그인이 아닙니다."),

    // 로그인
    LOGIN_MISMATCH(false, 2400, "이메일과 비밀번호가 일치하지 않습니다."),


    /*
        3000 : Response 오류
    */
    // Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),


    /*
        4000 : Database, Server 오류
    */
    // Common
    DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, 4001, "서버와의 연결에 실패하였습니다.");


    private final boolean isSuccess;
    private final int code;
    private final String message;

    BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
