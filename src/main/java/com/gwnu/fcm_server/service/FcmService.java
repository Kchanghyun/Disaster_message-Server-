package com.gwnu.fcm_server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {
    private Set<String> deviceTokens = new HashSet<>();

    @Value("${fcm.service-account-file}")
    private String serviceAccountFilePath;

    @Value("${fcm.project-id}")
    private String projectId;

    @Value("${fcm.topic-name}")
    private String topicName;



    @PostConstruct
    public void initialize() throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(serviceAccountFilePath).getInputStream()))
                .setProjectId(projectId)
                .build();


        FirebaseApp.initializeApp(options);
    }

//    public void saveToken(String token) {
//        deviceTokens.add(token);
//        log.info("Token saved: " + token);
//    }
//
//    public void sendMessageToAllDevices(String title, String body) throws FirebaseMessagingException {
//        for (String token : deviceTokens) {
//            sendMessageByToken(title, body, token);
//        }
//    }

    public void sendMessageByTopic(String title, String body) throws IOException, FirebaseMessagingException {
        FirebaseMessaging.getInstance().send(Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setTopic(topicName)
                .build());
    }

//    public void sendMessageByToken(String title, String body,String token) throws FirebaseMessagingException{
//        FirebaseMessaging.getInstance().send(Message.builder()
//                    .setNotification(Notification.builder()
//                        .setTitle(title)
//                        .setBody(body)
//                        .build())
//                    .setToken(token)
//                .build());
//    }

//    public void sendMessageToSavedTokens(String title, String body) throws FirebaseMessagingException {
//        for (String token : deviceTokens) {
//            sendMessageByToken(title, body, token);
//        }
//    }
}

