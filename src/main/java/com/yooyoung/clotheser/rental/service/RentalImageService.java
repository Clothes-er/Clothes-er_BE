package com.yooyoung.clotheser.rental.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalImg;
import com.yooyoung.clotheser.rental.repository.RentalImgRepository;
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
import java.util.stream.Collectors;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class RentalImageService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final RentalImgRepository rentalImgRepository;

    /* 대여글 이미지 저장 */
    public List<String> uploadImages(MultipartFile[] images, Rental rental) throws BaseException {
        List<String> imgUrls = new ArrayList<>();

        // 대여글 이미지 없는 경우 (1: Swaager, 2: Postman)
        if (images.length == 0 || images[0].isEmpty()) {
            return null;
        }

        try {
            for (MultipartFile image : images) {
                // 파일명 중복 방지 위해 랜덤 숫자 추가
                String fileName = "rentals/" + UUID.randomUUID() + "_" + image.getOriginalFilename();

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(image.getSize());
                metadata.setContentType(image.getContentType());

                amazonS3.putObject(bucket, fileName, image.getInputStream(), metadata);
                String imgUrl = amazonS3.getUrl(bucket, fileName).toString();
                String decodedUrl = URLDecoder.decode(imgUrl, StandardCharsets.UTF_8);  // 한글로 변환

                imgUrls.add(decodedUrl);

                // 데이터베이스에 이미지 URL 저장
                RentalImg rentalImg = RentalImg.builder()
                        .imgUrl(decodedUrl)
                        .rental(rental)
                        .build();

                rentalImgRepository.save(rentalImg);
            }
        } catch (IOException e) {
            throw new BaseException(S3_UPLOAD_ERROR, INTERNAL_SERVER_ERROR);
        }

        return imgUrls;
    }

    /* 대여글 이미지 삭제 */
    public void deleteImages(List<RentalImg> rentalImgs) {
        // S3에서 삭제
        for (RentalImg rentalImg : rentalImgs) {
            String fileName = rentalImg.getImgUrl().substring(rentalImg.getImgUrl().lastIndexOf("/") + 1);
            amazonS3.deleteObject(bucket, "rentals/" + fileName);
        }

        // DB에서 삭제
        List<Long> rentalImgIds = rentalImgs.stream()
                .map(RentalImg::getId)
                .collect(Collectors.toList());
        rentalImgRepository.deleteAllByIdInBatch(rentalImgIds);
    }
}

