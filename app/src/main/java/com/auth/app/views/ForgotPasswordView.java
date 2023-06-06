package com.auth.app.views;

import com.auth.app.DTO.EmailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


@Route("forgot-password")
@PageTitle("Password Reset")
public class ForgotPasswordView extends VerticalLayout{
    private final OkHttpClient okHttpClient;

    public ForgotPasswordView(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;

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
        recoveryButton.addClickListener(e -> {
            try {
                initForgotPassword(emailField.getValue());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });

        form.add(title, emailField, recoveryButton);

        add(form);

    }
    private void initForgotPassword(String email) throws JsonProcessingException {
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

        UI.getCurrent().navigate("login");
    }
}
