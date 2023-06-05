package com.auth.app.views;

import com.auth.app.DTO.AuthenticationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Route("login")
@PageTitle("Login")
public class LoginView extends VerticalLayout {
    private final RestTemplate restTemplate;

    public LoginView(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        setClassName("container");
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        setSizeFull();

        FlexLayout form = new FlexLayout();
        form.setClassName("form");
        form.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        form.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Login");

        TextField emailField = new TextField("Email");
        emailField.setRequired(true);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequired(true);

        Button loginButton = new Button("Login");
        loginButton.addClickListener(e -> {
            authenticate(emailField.getValue(), passwordField.getValue()); // Call your authentication logic here
        });

        loginButton.setWidthFull(); // Set the login button width to match the password field

        Anchor registerLink = new Anchor("register", "Register");
        Anchor forgotPasswordLink = new Anchor("forgot-password", "Forgot password?");

        form.add(title, emailField, passwordField, loginButton, registerLink, forgotPasswordLink);

        add(form);
    }
    private void authenticate(String email, String password) {
        String authenticateUrl = "http://localhost:8080/api/auth/authenticate";

        AuthenticationRequest request = new AuthenticationRequest(email, password);

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(authenticateUrl, request, String.class);
            HttpStatusCode statusCode = responseEntity.getStatusCode();

            if (statusCode.is2xxSuccessful()) {
                String responseBody = responseEntity.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                if (jsonNode != null) {
                    String token = jsonNode.get("token").asText();

                    // Store the token in the local storage of the browser
                    Page page = UI.getCurrent().getPage();
                    page.executeJs("localStorage.setItem('token', $0)", token);

                    // Redirect to another view or perform any additional actions
                    UI.getCurrent().navigate("dashboard");
                } else {
                    System.out.println("Empty response body");
                }
            } else {
                System.out.println("Request failed with status code: " + statusCode);
            }
        } catch (HttpClientErrorException ex) {
            System.out.println("HTTP error occurred: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("An error occurred: " + ex.getMessage());
        }
    }


}
