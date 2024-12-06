package com.gwnu.fcm_server;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gwnu.fcm_server.Controller.FcmController;
import com.gwnu.fcm_server.Dto.MessageRequestDTO;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling // 스케줄링 기능 활성화
public class FcmServerApplication implements CommandLineRunner {
	@Autowired
	private FcmController fcmController;

	@Autowired
	private Server server; // Server 클래스 주입

	public static void main(String[] args) {
		SpringApplication.run(FcmServerApplication.class, args);
	}

	@Override
	public void run(String... args) throws IOException, FirebaseMessagingException {

		new Thread(() -> {
			try {
				server.startMonitoring();
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass())
						.error("모니터링 중 오류 발생", e);
			}
		}).start();
	}
}
