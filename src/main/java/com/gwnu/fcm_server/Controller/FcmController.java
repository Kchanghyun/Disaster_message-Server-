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

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FcmController {
    @Autowired
    private FcmService fcmService;

    @PostMapping("/api/fcm/message/send")
    public ResponseEntity sendMessageTopic(@RequestBody MessageRequestDTO requestDTO) throws IOException, FirebaseMessagingException{
        fcmService.sendMessageByTopic(requestDTO.title, requestDTO.body);
        return ResponseEntity.ok().build();
    }
}
