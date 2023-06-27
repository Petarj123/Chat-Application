# Chat Application

This project is a robust chat application built on Java using Spring Boot. The application has custom JWT (JSON Web Token) authentication and authorization integrated with a custom JWT filter to ensure secure communication. The chat features are facilitated by Socket.IO for real-time bidirectional communication.

Features

User Authentication and Authorization: The application includes robust user authentication and authorization implemented using JWT. This adds an extra layer of security as the server can validate the authenticity of clients by verifying the JWT.

Custom JWT Filter: The JWT filter intercepts incoming requests to secure endpoints and validates the included JWT. This feature ensures that only authenticated and authorized users can access protected resources.

Real-time Chat: The application uses Socket.IO to facilitate real-time bidirectional communication. Users can send and receive messages in real time, which significantly enhances the user experience.

Chat Room Creation: Users can create their own chat rooms. Upon creation, the chat room is saved in the database and the creating user is automatically added as a participant.

Invitation Management: Users can invite other users to join their chat rooms by generating an invitation link. Invitations are stored in the database until they're accepted or expire (after 15 minutes).

Message Management: Users can send text or voice messages to the chat room. Messages are stored in the database, which enables history tracking and future retrieval.

Participant Management: A user can view all the participants in a chat room they're part of. This allows users to see who is present in the chat room.

Usage

Registration: New users can register by providing their username and password. Upon successful registration, the server responds with a JWT that the user should include in the headers of their subsequent requests to access protected resources.

Login: Existing users can login by providing their username and password. Upon successful login, the server responds with a JWT.

Chat Room Creation: Users can create chat rooms by providing a room name. The server then adds the chat room to the database and the user to the room's participants.

Invitation Creation and Acceptance: Users can create an invitation for a chat room they're a participant in, and the server responds with an invitation link. Other users can accept the invitation by providing the link, and the server then adds them to the chat room's participants.

Messaging: Users can send text or voice messages to chat rooms they're a participant in. The server then adds the messages to the chat room's message history.

Viewing Participants: Users can view the participants in a chat room they're a part of. The server responds with the list of participant's usernames.
