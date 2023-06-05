package com.auth.app.views;

import com.auth.app.DTO.RegistrationRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Route("register")
@PageTitle("Register")
public class RegisterView extends VerticalLayout {
    private final RestTemplate restTemplate;

    public RegisterView(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

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
            register(emailField.getValue(), passwordField.getValue(), confirmPasswordField.getValue());
        });

        Anchor loginLink = new Anchor("login", "Already have an account? Login");

        form.add(title, emailField, passwordField, confirmPasswordField, registerButton, loginLink);

        add(form);
    }
    private void register(String email, String password, String confirmPassword) {
        String registerUrl = "http://localhost:8080/api/auth/register";

        RegistrationRequest request = new RegistrationRequest(email, password, confirmPassword);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(registerUrl, request, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                Notification.show("Registration successful!", 3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Registration failed.", 3000, Notification.Position.MIDDLE);
            }
        } catch (RestClientException e) {
            Notification.show("Registration failed.", 3000, Notification.Position.MIDDLE);
        }
    }
}
