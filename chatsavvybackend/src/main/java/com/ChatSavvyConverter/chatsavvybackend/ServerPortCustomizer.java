package com.ChatSavvyConverter.chatsavvybackend;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        String portStr = System.getenv("X_ZOHO_CATALYST_LISTEN_PORT");
        int listenPort = 9000; // Default port

        try {
            if (portStr != null && !portStr.isEmpty()) {
                listenPort = Integer.parseInt(portStr);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number in environment variable. Using default port " + listenPort);
        }

        factory.setPort(listenPort);
    }
}
