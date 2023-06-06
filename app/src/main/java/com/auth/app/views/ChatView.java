package com.auth.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("chat")
@PageTitle("Chat App")
@CssImport("./styles/style.css")
public class ChatView extends Div{
    private Div chatRoom;
    private Div chatRoomInput;

    public ChatView() {
        setClassName("container");

        Div lobby = createLobby();
        lobby.setClassName("lobby");

        chatRoom = new Div();
        chatRoom.setClassName("chat-room");

        chatRoomInput = createChatRoomInput();
        chatRoomInput.setClassName("chat-room-input");
        chatRoomInput.setVisible(false); // set invisible at the beginning

        add(lobby, chatRoom, chatRoomInput);
    }

    private Div createLobby() {
        Div lobby = new Div();
        for (int i = 0; i < 5; i++) {
            Div room = new Div();
            room.setClassName("room");
            room.add(new H3("Chat Room " + (i + 1)));
            int finalI = i;
            room.addClickListener(event -> {
                openChatRoom("Chat Room " + (finalI + 1));
                chatRoomInput.setVisible(true); // show chat room input when a room is clicked
            });
            lobby.add(room);
        }
        return lobby;
    }

    private void openChatRoom(String roomName) {
        chatRoom.removeAll();
        chatRoom.add(new H3(roomName));
        for (int i = 0; i < 10; i++) {
            chatRoom.add(new Paragraph("Message " + (i + 1)));
        }
    }

    private Div createChatRoomInput() {
        Div inputArea = new Div();
        TextField input = new TextField();
        Button sendButton = new Button("Send");
        sendButton.addClickListener(event -> {
            chatRoom.add(new Paragraph(input.getValue()));
            input.clear();
        });
        inputArea.add(input, sendButton);
        return inputArea;
    }
}

