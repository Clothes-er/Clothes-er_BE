package com.yooyoung.clotheser.clothes.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.yooyoung.clotheser.clothes.domain.Clothes;
import com.yooyoung.clotheser.clothes.domain.ClothesImg;
import com.yooyoung.clotheser.clothes.repository.ClothesImgRepository;
import com.yooyoung.clotheser.global.entity.BaseException;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.S3_UPLOAD_ERROR;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
public class ClothesImageService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final ClothesImgRepository clothesImgRepository;

    /* 보유 옷 이미지 저장 */
    public List<String> uploadClothesImages(MultipartFile[] images, Clothes clothes) throws BaseException {
        List<String> imgUrls = new ArrayList<>();

        // 보유 옷 이미지 없는 경우 (1: Swaager, 2: Postman)
        if (images.length == 0 || images[0].isEmpty()) {
            return null;
        }

        try {
            for (MultipartFile image : images) {
                // 파일명 중복 방지 위해 랜덤 숫자 추가
                String fileName = "clothes/" + UUID.randomUUID() + "_" + image.getOriginalFilename();

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(image.getSize());
                metadata.setContentType(image.getContentType());

                amazonS3.putObject(bucket, fileName, image.getInputStream(), metadata);
                String imgUrl = amazonS3.getUrl(bucket, fileName).toString();
                String decodedUrl = URLDecoder.decode(imgUrl, StandardCharsets.UTF_8);  // 한글로 변환

                imgUrls.add(decodedUrl);

                // 데이터베이스에 이미지 URL 저장
                ClothesImg clothesImg = ClothesImg.builder()
                        .imgUrl(decodedUrl)
                        .clothes(clothes)
                        .build();

                clothesImgRepository.save(clothesImg);
            }
        } catch (IOException e) {
            throw new BaseException(S3_UPLOAD_ERROR, INTERNAL_SERVER_ERROR);
        }

        return imgUrls;
    }

    /* 보유 옷 이미지 삭제 */
    public void deleteClothesImages(List<ClothesImg> clothesImgs) {
        // S3에서 삭제
        for (ClothesImg clothesImg : clothesImgs) {
            String fileName = clothesImg.getImgUrl().substring(clothesImg.getImgUrl().lastIndexOf("/") + 1);
            amazonS3.deleteObject(bucket, "clothes/" + fileName);
        }

        // DB에서 삭제
        List<Long> clothesImgIds = clothesImgs.stream()
                .map(ClothesImg::getId)
                .toList();
        clothesImgRepository.deleteAllByIdInBatch(clothesImgIds);
    }


}
