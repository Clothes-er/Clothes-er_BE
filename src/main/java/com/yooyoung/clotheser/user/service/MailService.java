package com.yooyoung.clotheser.user.service;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.global.util.RedisUtil;
import com.yooyoung.clotheser.user.dto.request.EmailCheckRequest;
import com.yooyoung.clotheser.user.dto.request.EmailRequest;
import com.yooyoung.clotheser.user.repository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${spring.mail.username}" + "@naver.com")
    private String senderEmail;

    private int authCode;

    private final UserRepository userRepository;

    // 임의의 6자리 양수 반환
    public void createAuthCode() {
        // 100000 ~ 999999
        authCode = (int)(Math.random() * (900000)) + 100000;
    }

    // 이메일 작성
    public MimeMessage createEmail(String email) throws BaseException {

        // 인증 번호 생성
        createAuthCode();

        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            // 이메일 메시지 설정 (ex. multipart 형식 지원, 문자 인코딩)
            MimeMessageHelper helper = new MimeMessageHelper(message, true,"utf-8");

            // 전송자, 수신자
            helper.setFrom(senderEmail);
            helper.setTo(email);

            // 제목
            helper.setSubject("[Clothes:er] 회원가입 인증 번호입니다.");

            // 내용 (HTML 형식으로 작성)
            String content = "안녕하세요.<br>" +
                    "Clothes:er를 방문해주셔서 진심으로 감사드립니다.<br><br>" +
                    "서비스를 이용하기 위해서는 이메일 인증이 필요합니다.<br>" +
                    "아래의 인증 번호를 입력하여 회원가입을 완료해주세요.<br>" +
                    "<br><br><h1>" + authCode + "</h1><br><br>" +
                    "감사합니다.";
            helper.setText(content,true);   // true: html 설정
        } catch (MessagingException e) {    // 이메일 서버에 연결할 수 없거나 인증 오류가 발생하는 등 오류
            throw new BaseException(EMAIL_SERVER_ERROR, INTERNAL_SERVER_ERROR);
        }

        return message;
    }

    // 이메일 인증 번호 전송
    public BaseResponseStatus sendEmail(EmailRequest emailRequest) throws BaseException {

        String email = emailRequest.getEmail();

        // 이메일 중복 확인
        if (userRepository.existsByEmailAndDeletedAtNull(email)) {
            throw new BaseException(EMAIL_EXISTS, CONFLICT);
        }

        // 메일 전송
        MimeMessage message = createEmail(email);
        javaMailSender.send(message);

        // 인증 번호 유효 시간 설정 (5분)
        redisUtil.setDataExpire(Integer.toString(authCode),email, 60 * 5L);

        return SUCCESS;
    }

    // 이메일 인증 번호 검증
    public BaseResponseStatus checkEmail(EmailCheckRequest emailCheckRequest) throws BaseException {

        String authCode = String.valueOf(emailCheckRequest.getAuthCode());
        String email = emailCheckRequest.getEmail();

        // Redis에 인증 번호가 없는 경우 (ex. 인증 번호를 발급 받지 않음, 유효 시간이 지남)
        if (redisUtil.getData(authCode) == null) {
            throw new BaseException(INVALID_AUTH_CODE, BAD_REQUEST);
        }

        // 이메일과 인증 번호가 일치하는지 확인
        if (redisUtil.getData(authCode).equals(email)) {
            return SUCCESS;
        }
        else {
            throw new BaseException(FAILED_TO_CHECK_EMAIL, BAD_REQUEST);
        }

    }

}
