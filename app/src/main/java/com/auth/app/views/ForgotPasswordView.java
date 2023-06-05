package com.auth.app.views;

import com.auth.app.DTO.EmailRequest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


@Route("forgot-password")
@PageTitle("Password Reset")
public class ForgotPasswordView extends VerticalLayout{
    private final RestTemplate restTemplate;

    public ForgotPasswordView(RestTemplate restTemplate) {
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

        H2 title = new H2("Password Recovery");

        TextField emailField = new TextField("Email");
        emailField.setRequired(true);

        Button recoveryButton = new Button("Recover password");
        recoveryButton.addClickListener(e -> initForgotPassword(emailField.getValue()));

        form.add(title, emailField, recoveryButton);

        add(form);

    }
    private void initForgotPassword(String email){
        String url = "http://localhost:8080/api/auth/recovery";

        EmailRequest request = new EmailRequest(email);

        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        UI.getCurrent().navigate("login");
    }
}
