package com.gwnu.fcm_server.Dto;

import lombok.*;

@AllArgsConstructor
@Data
public class MessageRequestDTO {
    public String title;
    public String body;
    public String location;
    public String targetToken;
}
