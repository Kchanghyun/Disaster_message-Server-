package com.gwnu.fcm_server.Controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gwnu.fcm_server.Dto.MessageRequestDTO;
import com.gwnu.fcm_server.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FcmController {
    @Autowired
    private FcmService fcmService;

    // Postman 전용
    @PostMapping(value = "/api/fcm/message/send", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> sendMessageTopic(@RequestBody MessageRequestDTO requestDTO) {
        try {
            fcmService.sendMessageByTopic(requestDTO.title, requestDTO.body, requestDTO.location);
            log.info("{},{},{}", requestDTO.title, requestDTO.body, requestDTO.location);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.internalServerError().body("Failed to send message");
        }
    }
}
