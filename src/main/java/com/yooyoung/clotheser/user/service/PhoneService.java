package com.yooyoung.clotheser.user.service;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.global.util.RedisUtil;
import com.yooyoung.clotheser.user.dto.request.PhoneCheckRequest;
import com.yooyoung.clotheser.user.dto.request.PhoneRequest;
import com.yooyoung.clotheser.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.PHONE_NUMBER_EXISTS;
import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.SUCCESS;
import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@Transactional
@RequiredArgsConstructor
public class PhoneService {

    @Value("${coolsms.api.key}")
    private String apiKey;
    @Value("${coolsms.api.secret}")
    private String apiSecretKey;
    @Value("${coolsms.api.senderPhone}")
    private String senderPhone;

    @Autowired
    private RedisUtil redisUtil;

    private int authCode;

    private final UserRepository userRepository;
    private final String PHONE = "phone: ";

    private DefaultMessageService messageService;

    // 임의의 6자리 양수 반환
    public void createAuthCode() {
        // 100000 ~ 999999
        authCode = (int)(Math.random() * (900000)) + 100000;
    }

    @PostConstruct
    private void init(){
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecretKey, "https://api.coolsms.co.kr");
    }

    // 문자 메시지 작성
    public void sendPhoneMessage(String to) {
        Message message = new Message();

        // 인증 번호 생성
        createAuthCode();

        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 함
        message.setFrom(senderPhone);
        message.setTo(to);
        message.setText("[Clothes:er]\n본인 확인을 위해 인증 번호를 입력해주세요. [" + authCode + "]");

        this.messageService.sendOne(new SingleMessageSendingRequest(message));
    }

    // 휴대폰 인증 번호 전송
    public BaseResponseStatus sendPhone(PhoneRequest phoneRequest) throws BaseException {

        // 휴대폰 번호 중복 확인
        if (userRepository.existsByPhoneNumberAndDeletedAtNull(phoneRequest.getPhoneNumber())) {
            throw new BaseException(PHONE_NUMBER_EXISTS, CONFLICT);
        }

        // 수신 번호 형태에 맞춰 '-' 제거
        String phoneNumber = phoneRequest.getPhoneNumber().replaceAll("-", "");

        // 휴대폰 인증 번호 전송
        sendPhoneMessage(phoneNumber);

        // 인증 번호 유효 시간 설정 (3분)
        redisUtil.setDataExpire(PHONE + authCode, phoneNumber, 60 * 3L);

        return SUCCESS;
    }

    // 휴대폰 인증 번호 검증
    public BaseResponseStatus checkPhone(PhoneCheckRequest phoneCheckRequest) throws BaseException {

        String authCode = String.valueOf(phoneCheckRequest.getAuthCode());


        return SUCCESS;

    }

}
