package com.gwnu.fcm_server.Controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gwnu.fcm_server.Dto.MessageRequestDTO;
//import com.gwnu.fcm_server.Dto.TokenRequestDTO;
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

    // postman으로 보내면 이걸로 보내야 함.
//    @PostMapping("/message/fcm/token")
//    public ResponseEntity sendMessageToken(@RequestBody MessageRequestDTO requestDTO) throws IOException, FirebaseMessagingException{
//        fcmService.sendMessageByToken(requestDTO.title, requestDTO.body, requestDTO.targetToken);
//        return ResponseEntity.ok().build();
//    }
//
//    // 토큰 등록용 엔드포인트
//    @PostMapping("/api/fcm/token/register")
//    public ResponseEntity receiveToken(@RequestBody TokenRequestDTO tokenRequest) throws FirebaseMessagingException {
//        log.info("received FCM token: " + tokenRequest.getToken()); // 성공
//        fcmService.saveToken(tokenRequest.getToken()); // 성공
//
//        fcmService.sendMessageToAllDevices("hello", "hi"); // 성공
//        return ResponseEntity.ok().build();
//    }
}
