package com.auth.app.views;

import com.auth.app.DTO.EmailRequest;
import com.auth.app.DTO.RegistrationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import okhttp3.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Route("register")
@PageTitle("Register")
public class RegisterView extends VerticalLayout {
    private final OkHttpClient okHttpClient;

    public RegisterView(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;

        setClassName("container");
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        setSizeFull();

        FlexLayout form = new FlexLayout();
        form.setClassName("form");
        form.setFlexDirection(FlexLayout.FlexDirection.COLUMN);

        H2 title = new H2("Register");

        TextField emailField = new TextField("Email");
        emailField.setRequired(true);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequired(true);

        PasswordField confirmPasswordField = new PasswordField("Confirm Password");
        confirmPasswordField.setRequired(true);

        Button registerButton = new Button("Register");
        registerButton.addClickListener(e -> {
            try {
                register(emailField.getValue(), passwordField.getValue(), confirmPasswordField.getValue());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });

        Anchor loginLink = new Anchor("login", "Already have an account? Login");

        form.add(title, emailField, passwordField, confirmPasswordField, registerButton, loginLink);

        add(form);
    }
    private void register(String email, String password, String confirmPassword) throws JsonProcessingException {
        String url = "http://localhost:8080/api/auth/recovery";
        ObjectMapper objectMapper = new ObjectMapper();
        EmailRequest request = new EmailRequest(email);

        String json = objectMapper.writeValueAsString(request);
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(json, mediaType);

        Request httpRequest = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try(Response httpResponse = okHttpClient.newCall(httpRequest).execute()) {
            if (httpResponse.isSuccessful()) {
                Notification.show("Registration successful!", 3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate("login");
            } else {
                Notification.show("Registration failed.", 3000, Notification.Position.MIDDLE);
            }
        } catch (IOException e) {
            Notification.show("Registration failed.", 3000, Notification.Position.MIDDLE);
        }
    }
}
