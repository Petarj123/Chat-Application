# Chat Application
 
This project is a chat application built on Java using Spring Boot. The application has custom JWT (JSON Web Token) authentication and authorization integrated with a custom JWT filter to ensure secure communication.

Features

User Authentication and Authorization: The application includes robust user authentication and authorization implemented using JWT. This adds an extra layer of security as the server can validate the authenticity of clients by verifying the JWT.

Custom JWT Filter: The JWT filter intercepts incoming requests to secure endpoints and validates the included JWT. This feature ensures that only authenticated and authorized users can access protected resources.

Real-time Chat: The application uses Socket.IO to facilitate real-time bidirectional communication. Users can send and receive messages in real time, which significantly enhances the user experience.
