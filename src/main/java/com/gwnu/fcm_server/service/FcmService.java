package com.gwnu.fcm_server.service;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

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

    public void sendMessageByTopic(String title, String body) throws IOException, FirebaseMessagingException {
        Message message = Message.builder()
                .putData("title", title)
                .putData("body", body)
                .setTopic(topicName)
                .build();

        FirebaseMessaging.getInstance().send(message);
    }
}

