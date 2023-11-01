package com.auth.app.config;

import com.auth.app.jwt.service.CustomUserDetails;
import com.auth.app.jwt.service.JwtService;
import com.auth.app.model.user.model.User;
import com.auth.app.model.user.repository.UserRepository;
import com.corundumstudio.socketio.SocketIOServer;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Date;

@CrossOrigin
@Configuration
@Log4j2
@RequiredArgsConstructor
public class SocketIOConfig {
    @Value("${socket.host}")
    private String HOST;
    @Value("${socket.port}")
    private int PORT;
    private SocketIOServer server;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    @Bean
    public SocketIOServer socketIOServer(){
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(HOST);
        config.setPort(PORT);
        server = new SocketIOServer(config);

        server.addConnectListener(socketIOClient -> {
            final String header = socketIOClient.getHandshakeData().getUrlParams().get("token").get(0);
            if (header == null || !header.startsWith("Bearer ")) {
                socketIOClient.disconnect();
                return;
            }

            String token = header.substring(7);
            String username;

            try {
                if (jwtService.validateToken(token)) {
                    username = jwtService.getUsername(token);
                    System.out.println(username + " connected to " + socketIOClient.getSessionId());
                } else {
                    try {
                        username = jwtService.getUsername(token);
                    } catch (ExpiredJwtException e) {
                        socketIOClient.disconnect();
                        return;
                    }

                    if (username != null) {
                        User user = userRepository.findByUsername(username).orElseThrow();
                        String refreshToken = user.getRefreshToken();

                        if (refreshToken != null) {
                            if (!jwtService.validateRefreshToken(refreshToken)) {
                                socketIOClient.disconnect();
                                return;
                            }

                            Date refreshTokenExpiry = jwtService.extractExpirationDate(refreshToken);

                            if (refreshTokenExpiry.before(new Date())) {
                                socketIOClient.disconnect();
                            } else {
                                // The refresh token is still valid, renew the JWT
                                String newToken = jwtService.generateToken(username);
                                socketIOClient.getHandshakeData().getHttpHeaders().set("Authorization", "Bearer " + newToken);
                                log.debug("User reconnected with new JWT to " + socketIOClient.getSessionId());
                            }
                        } else {
                            socketIOClient.disconnect();
                        }
                    } else {
                        socketIOClient.disconnect();
                    }
                }
            } catch (Exception e) {
                socketIOClient.disconnect();
                log.error("Error during authentication", e);
            }
        });

        server.addDisconnectListener(socketIOClient -> log.debug("User disconnected with socket " + socketIOClient.getSessionId()));

        server.start();

        return server;
    }
    @PreDestroy
    public void stopSocketIOServer(){
        this.server.stop();
    }
}
