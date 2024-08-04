package com.yooyoung.clotheser.clothes.service;

import com.yooyoung.clotheser.clothes.domain.Clothes;
import com.yooyoung.clotheser.clothes.dto.ClothesRequest;
import com.yooyoung.clotheser.clothes.dto.ClothesResponse;
import com.yooyoung.clotheser.clothes.repository.ClothesRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.global.util.Base64UrlSafeUtil;
import com.yooyoung.clotheser.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.FAIL_TO_ENCRYPT;
import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.REQUEST_FIRST_LOGIN;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@Transactional
@RequiredArgsConstructor
public class ClothesService {

    @Autowired
    private AESUtil aesUtil;
    @Value("${aes.key}")
    private String AES_KEY;

    private final ClothesRepository clothesRepository;
    private final ClothesImageService clothesImageService;

    /* 보유 옷 생성 */
    public ClothesResponse createClothes(ClothesRequest clothesRequest, MultipartFile[] images, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 보유 옷 저장
        Clothes clothes = clothesRequest.toEntity(user);
        clothesRepository.save(clothes);

        // 보유 옷 이미지 저장
        List<String> imgUrls = clothesImageService.uploadClothesImages(images, clothes);

        // 본인의 id 암호화하기
        String userSid;
        try {
            String encodedUserId = aesUtil.encrypt(String.valueOf(user.getId()), AES_KEY);
            userSid = Base64UrlSafeUtil.encode(encodedUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
        }

        return new ClothesResponse(user, userSid, clothes, imgUrls);
    }

}
