package com.ChatSavvyConverter.chatsavvybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ChatsavvybackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatsavvybackendApplication.class, args);
	}

}

@RestController  
class TestController {
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Test successful", HttpStatus.OK);
    }
}
