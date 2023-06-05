package com.auth.app.views;

import com.auth.app.DTO.AuthenticationRequest;
import com.auth.app.DTO.AuthenticationResponse;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.web.client.RestTemplate;

@Route("login")
public class LoginView extends VerticalLayout {
    private final RestTemplate restTemplate;


    public LoginView(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        setClassName("container");

        Div form = new Div();
        form.setClassName("form");

        H2 title = new H2("Login");

        TextField emailField = new TextField("Email");
        emailField.setRequired(true);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequired(true);

        Button loginButton = new Button("Login");
        loginButton.addClickListener(e -> {
            authenticate(emailField.getValue(), passwordField.getValue()); // Call your authentication logic here
        });

        Anchor registerLink = new Anchor("register", "Register");
        Anchor forgotPasswordLink = new Anchor("forgot-password", "Forgot password?");

        form.add(title, emailField, passwordField, loginButton, registerLink, forgotPasswordLink);

        add(form);
    }
    private void authenticate(String email, String password) {
        String baseUrl = "http://localhost:8080/api/auth";
        String authenticateUrl = baseUrl + "/authenticate";

        AuthenticationRequest request = new AuthenticationRequest(email, password);
        AuthenticationResponse response = restTemplate.postForObject(authenticateUrl, request, AuthenticationResponse.class);

        String token = response.getToken();

        // Store the token in a browser cookie
        VaadinServletResponse vaadinServletResponse = (VaadinServletResponse) VaadinService.getCurrentResponse();
        Cookie cookie = new Cookie("jwtToken", token);
        cookie.setPath("/");
        cookie.setMaxAge(3600); // Set the expiration time for the cookie, in seconds
        vaadinServletResponse.addCookie(cookie);

        // Redirect to another view or perform any additional actions
        UI.getCurrent().navigate("dashboard");
    }
}
