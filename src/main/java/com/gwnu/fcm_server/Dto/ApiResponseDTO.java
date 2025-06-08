package com.gwnu.fcm_server.Dto;

import com.gwnu.fcm_server.Server;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponseDTO {
    private HeaderDTO header;
    private List<DisasterMessageDTO> body;
}
