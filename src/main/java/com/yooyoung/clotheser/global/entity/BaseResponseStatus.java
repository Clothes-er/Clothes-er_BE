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
    FORBIDDEN_ACCESS_JWT(false, 2002, "권한이 없는 유저의 접근입니다."),
    LOGOUT_JWT(false, 2003, "로그아웃된 JWT입니다."),
    EXPIRED_JWT(false, 2004, "만료된 JWT입니다."),
    INVALID_JWT(false, 2005, "유효하지 않은 JWT입니다."),
    FORBIDDEN_LOGIN_REFRESH_TOKEN(false, 2006, "리프레시 토큰으로 로그인 할 수 없습니다."),
    REQUEST_FIRST_LOGIN(false, 2007, "최초 로그인이 필요합니다."),
    FILE_TOO_LARGE(false, 2008, "업로드 할 수 있는 최대 총 파일의 크기는 50MB입니다."),
    FORBIDDEN_USER(false, 2009, "작성자만 접근할 수 있습니다."),
    EMPTY_JWT_CLAIMS(false, 2010, "잘못된 JWT입니다."),

    // 2. User (2100 ~ 2199)

    // 회원가입
    PASSWORD_CONFIRMATION_MISMATCH(false, 2110, "비밀번호가 일치하지 않습니다."),
    NICKNAME_EXISTS(false, 2111, "중복된 닉네임입니다."),
    EMAIL_EXISTS(false, 2112, "중복된 이메일입니다."),
    PHONE_NUMBER_EXISTS(false, 2113, "중복된 전화번호입니다."),

    // 최초 로그인
    IS_NOT_FIRST_LOGIN(false, 2120, "최초 로그인이 아닙니다."),

    // 로그인
    LOGIN_MISMATCH(false, 2130, "이메일과 비밀번호가 일치하지 않습니다."),
    USE_RESTRICTED(false, 2131, "서비스 이용이 제한되었습니다."),

    // 인증
    INVALID_AUTH_CODE(false, 2140, "유효하지 않은 인증 번호입니다."),
    FAILED_TO_CHECK_EMAIL(false, 2141, "이메일 인증에 실패하였습니다."),
    FAILED_TO_CHECK_PHONE(false, 2142, "휴대폰 인증에 실패하였습니다."),

    // 신고
    REPORT_USER_RENTAL_EXISTS(false, 2150, "거래 중인 경우 이용 제한을 할 수 없습니다. 유예 상태에서 거래 완료를 유도하세요."),

    // 회원 탈퇴
    WITHDRAW_USER_RENTAL_EXISTS(false, 2160, "거래 중인 경우 탈퇴할 수 없습니다."),

    // 3. Rental (2200 ~ 2299)
    EMPTY_CLOTHES_ID(false, 2200, "보유 옷 id가 필요합니다."),
    FORBIDDEN_CREATE_RENTAL_INFO(false, 2201, "판매자만 대여 정보를 입력할 수 있습니다."),
    TOO_MANY_RENTAL_IMAGES(false, 2203, "대여글 이미지는 최대 3장까지 첨부할 수 있습니다."),
    RENTAL_CHECK_EXISTS(false, 2204, "옷 상태는 한 번만 체크할 수 있습니다."),
    FORBIDDEN_CREATE_RENTAL_CHECK(false, 2205, "대여자만 옷 상태를 체크할 수 있습니다."),
    REQUEST_RENTAL_CHECK(false, 2206, "대여자가 옷 상태를 먼저 체크해야 대여할 수 있습니다."),
    FORBIDDEN_DELETE_RENTAL(false, 2207, "대여 중인 경우에는 대여글을 삭제할 수 없습니다."),
    REVIEW_EXISTS(false, 2208, "거래 후기는 한 번만 작성할 수 있습니다."),
    FORBIDDEN_CREATE_RENTAL(false, 2209, "보유 옷을 가진 회원만 해당 대여글을 작성할 수 있습니다."),
    FORBIDDEN_UPDATE_RENTAL(false, 2210, "보유 옷을 가진 회원만 해당 대여글을 수정할 수 있습니다."),
    CLOTHES_HAS_RENTAL(false, 2211, "해당 보유 옷은 이미 다른 대여글과 연동되어 있습니다."),

    // 4. Chat (2300 ~ 2399)
    FORBIDDEN_CREATE_CHAT_ROOM(false, 2300, "대여글 작성자는 채팅방을 만들 수 없습니다."),
    RENTAL_CHAT_ROOM_EXISTS(false, 2301, "해당 대여글의 채팅방이 이미 존재합니다."),
    FORBIDDEN_ENTER_CHAT_ROOM(false, 2302, "채팅방 참여자가 아닙니다."),
    REQUEST_EMPTY_MESSAGE(false, 2303, "메시지를 입력해주세요."),
    USER_CHAT_ROOM_EXISTS(false, 2304, "해당 유저 채팅방이 이미 존재합니다."),
    FORBIDDEN_CREATE_CHAT_ROOM_MYSELF(false, 2305, "본인은 본인과 채팅방을 만들 수 없습니다."),

    // 5. Clothes (2400 ~ 2499)
    TOO_MANY_CLOTHES_IMAGES(false, 2400, "보유 옷 이미지는 최대 3장까지 첨부할 수 있습니다."),
    FORBIDDEN_CREATE_CLOTHES(false, 2401, "대여글을 작성한 회원만 해당 보유 옷을 등록할 수 있습니다."),
    FORBIDDEN_UPDATE_CLOTHES(false, 2402, "대여글을 작성한 회원만 해당 보유 옷을 수정할 수 있습니다."),
    RENTAL_HAS_CLOTHES(false, 2403, "해당 대여글은 이미 다른 보유 옷과 연동되어 있습니다."),

    // 6. Like (2500 ~ 2599)
    FORBIDDEN_LIKE_MINE(false, 2500, "작성자 본인은 찜할 수 없습니다."),
    LIKE_EXISTS(false, 2501, "이미 찜한 옷입니다."),

    /*
        3000 : Response 오류
    */
    // 1. Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),

    // 2. User
    NOT_FOUND_USER(false, 3100, "회원을 찾을 수 없습니다."),
    NOT_FOUND_USER_BY_EMAIL(false, 3101, "이메일이 잘못되었습니다."),
    NOT_FOUND_DEVICE_TOKEN(false, 3102, "회원의 디바이스 토큰을 찾을 수 없습니다."),

    // 3. Rental
    NOT_FOUND_RENTAL(false, 3200, "대여글을 찾을 수 없습니다."),
    NOT_FOUND_RENTAL_INFO(false, 3201, "대여 정보를 찾을 수 없습니다."),
    NOT_FOUND_RENTAL_LIKE(false, 3202, "해당 대여글의 찜을 찾을 수 없습니다."),

    // 4. Chat
    NOT_FOUND_CHAT_ROOM(false, 3300, "채팅방을 찾을 수 없습니다."),
    NOT_FOUND_RENTAL_CHAT_ROOM(false, 3301, "해당 대여글 채팅방을 찾을 수 없습니다."),
    NOT_FOUND_USER_CHAT_ROOM(false, 3302, "해당 유저 채팅방을 찾을 수 없습니다."),

    // 5. Clothes
    NOT_FOUND_CLOTHES(false, 3400, "보유 옷을 찾을 수 없습니다."),
    NOT_FOUND_CLOTHES_LIKE(false, 3401, "해당 보유 옷의 찜을 찾을 수 없습니다."),

    // 6. Report
    NOT_FOUND_REPORT(false, 3500, "해당 신고를 찾을 수 없습니다."),

    /*
        4000 : Database, Server 오류
    */
    // Common
    DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, 4001, "서버와의 연결에 실패하였습니다."),
    S3_UPLOAD_ERROR(false, 4002, "S3 이미지 업로드에 실패하였습니다."),
    EMAIL_SERVER_ERROR(false, 4003, "이메일 전송 시 오류가 발생하였습니다."),
    FAIL_TO_ENCRYPT(false, 4004, "암호화에 실패하였습니다."),
    FAIL_TO_DECRYPT(false, 4005, "복호화에 실패하였습니다.");


    private final boolean isSuccess;
    private final int code;
    private final String message;

    BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
