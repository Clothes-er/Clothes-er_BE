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
    private final String EMAIL = "email: ";

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
            String content = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                    "  <head>\n" +
                    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                    "    <title>Clothes:er 이메일 인증</title>\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "    <table\n" +
                    "      style=\"\n" +
                    "        width: 628px;\n" +
                    "        box-sizing: border-box;\n" +
                    "        border-collapse: collapse;\n" +
                    "        background-color: #ffffff;\n" +
                    "        min-width: 628px;\n" +
                    "        min-height: 774px;\n" +
                    "        text-align: left;\n" +
                    "        margin: 0 auto;\n" +
                    "      \"\n" +
                    "    >\n" +
                    "      <tbody>\n" +
                    "        <tr>\n" +
                    "          <td>\n" +
                    "            <table\n" +
                    "              cellpadding=\"0\"\n" +
                    "              cellspacing=\"0\"\n" +
                    "              style=\"margin: 53px 42px 42px 62px\"\n" +
                    "            >\n" +
                    "              <tbody>\n" +
                    "                <tr>\n" +
                    "                  <td style=\"padding-bottom: 11.61px\">\n" +
                    "                     <img\n" +
                    "                      alt=\"\"\n" +
                    "                      src=\"https://ifh.cc/g/AhPdSm.png\"\n" +
                    "                      style=\"display: block\"\n" +
                    "                      width=\"196\"\n" +
                    "                      height=\"32\"\n" +
                    "                    />\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "                <tr>\n" +
                    "                  <td style=\"padding-top: 30px\">\n" +
                    "                    <span\n" +
                    "                      style=\"\n" +
                    "                        width: 628px;\n" +
                    "                        color: #5a42ee;\n" +
                    "                        font-family: Pretendard;\n" +
                    "                        font-size: 25px;\n" +
                    "                        font-style: normal;\n" +
                    "                        font-weight: 400;\n" +
                    "                        line-height: 150%;\n" +
                    "                      \"\n" +
                    "                    >\n" +
                    "                      인증코드를 확인해주세요\n" +
                    "                    </span>\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "                <tr>\n" +
                    "                  <td style=\"padding-top: 30px\">\n" +
                    "                    <span\n" +
                    "                      style=\"\n" +
                    "                        color: #5a42ee;\n" +
                    "                        font-family: Pretendard;\n" +
                    "                        font-size: 32px;\n" +
                    "                        font-style: normal;\n" +
                    "                        font-weight: 700;\n" +
                    "                        line-height: 150%;\n" +
                    "                        margin-bottom: 30px;\n" +
                    "                      \"\n" +
                    "                    >\n" + authCode +
                    "                      \n" +
                    "                    </span>\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "                <tr>\n" +
                    "                  <td style=\"padding-top: 30px\">\n" +
                    "                    <span\n" +
                    "                      style=\"\n" +
                    "                        color: #2d2d2d;\n" +
                    "                        font-family: Pretendard;\n" +
                    "                        font-size: 18px;\n" +
                    "                        font-style: normal;\n" +
                    "                        font-weight: 400;\n" +
                    "                        line-height: 150%;\n" +
                    "                      \"\n" +
                    "                    >\n" +
                    "                      안녕하세요.<br />\n" +
                    "                      Clothes:er를 방문해주셔서 진심으로 감사드립니다.<br /><br />\n" +
                    "                      서비스를 이용하기 위해서는 이메일 인증이 필요합니다.<br />\n" +
                    "                      아래의 인증 번호를 입력하여 회원가입을 완료해주세요.<br /><br />\n" +
                    "                      만약 본인 요청에 의한 이메일 인증이 아니라면,<br />\n" +
                    "                      고객센터 또는 clotheser@naver.com으로 관련 내용을\n" +
                    "                      전달해주세요.<br /><br />\n" +
                    "                      감사합니다.\n" +
                    "                    </span>\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "              </tbody>\n" +
                    "            </table>\n" +
                    "            <table\n" +
                    "              cellpadding=\"0\"\n" +
                    "              cellspacing=\"0\"\n" +
                    "              style=\"\n" +
                    "                width: 628px;\n" +
                    "                padding: 38px 0px 55px 62px;\n" +
                    "                background: #efecff;\n" +
                    "              \"\n" +
                    "            >\n" +
                    "              <tbody>\n" +
                    "                <tr>\n" +
                    "                  <td style=\"padding-top: 24px; padding-bottom: 12px\">\n" +
                    "                    <span\n" +
                    "                      style=\"\n" +
                    "                        height: 212px;\n" +
                    "                        color: #606060;\n" +
                    "                        font-family: Pretendard;\n" +
                    "                        font-size: 11px;\n" +
                    "                        font-style: normal;\n" +
                    "                        font-weight: 500;\n" +
                    "                        line-height: 150%;\n" +
                    "                      \"\n" +
                    "                    >\n" +
                    "                      본 메일은 발신 전용으로 회신되지 않습니다.<br />\n" +
                    "                      궁금하신 점은 클로저 고객센터를 통해 문의하시기\n" +
                    "                      바랍니다.<br /><br />\n" +
                    "                      Email: clotheser@naver.com<br /><br /><br />\n" +
                    "                      © Clothes:er All rights reserved clotheser.co.kr\n" +
                    "                    </span>\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "              </tbody>\n" +
                    "            </table>\n" +
                    "          </td>\n" +
                    "        </tr>\n" +
                    "      </tbody>\n" +
                    "    </table>\n" +
                    "  </body>\n" +
                    "</html>\n";
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
        redisUtil.setDataExpire(EMAIL + authCode, email, 60 * 5L);

        return SUCCESS;
    }

    // 이메일 인증 번호 검증
    public BaseResponseStatus checkEmail(EmailCheckRequest emailCheckRequest) throws BaseException {

        String authCode = String.valueOf(emailCheckRequest.getAuthCode());
        String email = emailCheckRequest.getEmail();

        // Redis에 인증 번호가 없는 경우 (ex. 인증 번호를 발급 받지 않음, 유효 시간이 지남)
        String storedEmail = redisUtil.getData(EMAIL + authCode);
        if (storedEmail == null) {
            throw new BaseException(INVALID_AUTH_CODE, BAD_REQUEST);
        }

        // 이메일과 인증 번호가 일치하는지 확인
        if (storedEmail.equals(email)) {
            return SUCCESS;
        }
        else {
            throw new BaseException(FAILED_TO_CHECK_EMAIL, BAD_REQUEST);
        }

    }

}
