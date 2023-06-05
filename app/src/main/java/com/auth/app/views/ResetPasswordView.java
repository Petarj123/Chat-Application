package com.auth.app.views;

import com.auth.app.DTO.PasswordRequest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Route("reset-password")
@PageTitle("Reset Password")
public class ResetPasswordView extends VerticalLayout {
    private final RestTemplate restTemplate;

    public ResetPasswordView(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        setClassName("container");
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setSizeFull();

        FlexLayout form = new FlexLayout();
        form.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        form.setAlignItems(FlexComponent.Alignment.CENTER);
        form.setSizeFull();
        form.setClassName("form");

        H2 title = new H2("Password Reset");

        PasswordField password = new PasswordField("Password");
        password.setRequired(true);

        PasswordField confirmPassword = new PasswordField("Confirm Password");
        confirmPassword.setRequired(true);

        Button resetButton = new Button("Reset password");
        resetButton.addClickListener(e -> resetPassword(password.getValue(), confirmPassword.getValue()));

        form.add(title, password, confirmPassword, resetButton);

        add(form);
    }
    // TODO FIX THIS
    private void resetPassword(String password, String confirmPassword) {
        String url = "http://localhost:8080/api/auth/reset";
        String currentUrl = UI.getCurrent().getPage().fetchCurrentURL();
        UriComponents components = UriComponentsBuilder.fromUriString(currentUrl).build();
        String token = components.getQueryParams().getFirst("token");
        String resetUrl = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("token", token)
                .toUriString();
        PasswordRequest request = new PasswordRequest(password, confirmPassword);
        restTemplate.put(resetUrl, request);

        UI.getCurrent().navigate("login");
    }
}
