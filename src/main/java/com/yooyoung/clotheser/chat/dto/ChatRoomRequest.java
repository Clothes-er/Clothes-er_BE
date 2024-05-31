package com.yooyoung.clotheser.chat.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class ChatRoomRequest {

    private Long id;
    private Long buyer;
    private Long lender;
    private Long rentalId;

}
