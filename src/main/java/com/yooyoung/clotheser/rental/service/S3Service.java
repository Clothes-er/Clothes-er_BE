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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final RentalImgRepository rentalImgRepository;

    public List<String> uploadImages(MultipartFile[] images, Rental rental) throws BaseException {
        List<String> imgUrls = new ArrayList<>();

        // 대여글 이미지 없는 경우
        if (images[0].isEmpty()) {
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
                imgUrls.add(imgUrl);

                // 데이터베이스에 이미지 URL 저장
                RentalImg rentalImg = RentalImg.builder()
                        .imgUrl(imgUrl)
                        .rental(rental)
                        .build();

                rentalImgRepository.save(rentalImg);
            }
        } catch (IOException e) {
            throw new BaseException(S3_UPLOAD_ERROR, INTERNAL_SERVER_ERROR);
        }

        return imgUrls;
    }
}

