package com.auth.app.config;

import com.auth.app.jwt.JwtService;
import com.auth.app.model.Admin;
import com.auth.app.model.User;
import com.auth.app.repository.AdminRepository;
import com.auth.app.repository.UserRepository;
import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Optional;

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
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Bean
    public SocketIOServer socketIOServer(){
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(HOST);
        config.setPort(PORT);
        server = new SocketIOServer(config);
        server.start();
        server.addConnectListener(socketIOClient -> {
            final String header = socketIOClient.getHandshakeData().getHttpHeaders().get("Authorization");
            System.out.println(header);
            if (header == null || !header.startsWith("Bearer ")) {
                socketIOClient.disconnect();
                return;
            }

            String jwt = header.substring(7);
            String username;
            String role;

            try {
                username = jwtService.extractEmail(jwt);
                role = jwtService.extractRole(jwt);
            } catch (Exception e) {
                socketIOClient.disconnect();
                return;
            }

            if (username != null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsernameAndRole(username, role);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.info("User connected to " + socketIOClient.getSessionId());
                } else if (jwtService.isTokenExpired(jwt)) {
                    try {
                        String refreshToken = getRefreshToken(username);
                        if (!jwtService.isTokenExpired(refreshToken)) {
                            String newJwt = jwtService.refreshJWTToken(refreshToken);
                            socketIOClient.getHandshakeData().getHttpHeaders().set("Authorization", "Bearer " + newJwt);
                            log.info("User connected to " + socketIOClient.getSessionId());
                        } else {
                            socketIOClient.disconnect();
                        }
                    } catch (IllegalAccessException e) {
                        socketIOClient.disconnect();
                    }
                } else {
                    socketIOClient.disconnect();
                }
            }
        });
        server.addDisconnectListener(socketIOClient -> log.info("User disconnected with socket " + socketIOClient.getSessionId()));
        return server;
    }
    private String getRefreshToken(String username) throws IllegalAccessException {
        Optional<User> existingUser = userRepository.findByEmail(username);
        Optional<Admin> existingAdmin = adminRepository.findByEmail(username);

        if (existingUser.isPresent()) {
            User user = existingUser.orElseThrow();
            return user.getRefreshToken();
        } else if (existingAdmin.isPresent()) {
            Admin admin = existingAdmin.orElseThrow();
            return admin.getRefreshToken();
        } else {
            throw new IllegalAccessException("Error getting refresh token");
        }
    }
    @PreDestroy
    public void stopSocketIOServer(){
        this.server.stop();
    }
}
