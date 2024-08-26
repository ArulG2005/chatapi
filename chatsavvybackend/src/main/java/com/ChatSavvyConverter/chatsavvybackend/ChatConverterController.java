package com.ChatSavvyConverter.chatsavvybackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/convert")
public class ChatConverterController {

    @Autowired
    private ChatConverterService chatConverterService;

    @PostMapping
    public ResponseEntity<byte[]> convertChat(@RequestBody ChatConversionRequest request) {
        System.out.println("Received conversion request: " + request.getLink() + ", Format: " + request.getFormat());

        try {
            byte[] fileBytes = chatConverterService.convertChat(request.getLink(), request.getFormat());

            if (fileBytes == null) {
                System.err.println("Conversion failed. No file bytes returned.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(request.getFormat()));
            headers.setContentDispositionFormData("attachment", "chat." + request.getFormat());

            System.out.println("Conversion successful. Returning file with format: " + request.getFormat());
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error during conversion: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private MediaType getMediaType(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return MediaType.APPLICATION_PDF;
            case "word":
                return MediaType.APPLICATION_OCTET_STREAM;
            case "png":
                return MediaType.IMAGE_PNG;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
