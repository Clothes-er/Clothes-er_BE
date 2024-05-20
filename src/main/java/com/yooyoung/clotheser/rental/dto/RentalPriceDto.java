package com.yooyoung.clotheser.rental.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RentalPriceDto {

    private int days;
    private int price;

}
