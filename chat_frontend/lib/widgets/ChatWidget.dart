import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:http/http.dart' as http;
import '../DTO/ChatRoomResponse.dart';



class ChatWidget extends StatefulWidget {
  const ChatWidget({super.key});

  @override
  _ChatWidgetState createState() => _ChatWidgetState();
}

class _ChatWidgetState extends State<ChatWidget> {
  List<ChatRoomDTO> chatRooms = [];
  List<MessageDTO>? messages;

  @override
  void initState() {
    super.initState();
    fetchChatRooms();
  }

  Future<List<ChatRoomDTO>> getAllChats() async {
    final prefs = await SharedPreferences.getInstance();
    Object? token = prefs.get('token');
    const url = 'http://localhost:8080/api/user/allChats';

    final headers = {'Authorization': 'Bearer $token'};

    final response = await http.get(Uri.parse(url), headers: headers);

    if (response.statusCode == 200) {
      final jsonData = json.decode(response.body) as List<dynamic>;
      final List<ChatRoomDTO> chatRooms = jsonData
          .map((json) => ChatRoomDTO.fromJson(json as Map<String, dynamic>))
          .toList();
      return chatRooms;
    } else {
      print('Request failed with status: ${response.statusCode}');
      return [];
    }
  }

  void fetchChatRooms() async {
    final List<ChatRoomDTO> fetchedChatRooms = await getAllChats();
    setState(() {
      chatRooms = fetchedChatRooms;
    });
    print("Successfully fetched");
  }

  Future<List<MessageDTO>> joinChatRoom(String id) async {
    final prefs = await SharedPreferences.getInstance();
    Object? token = prefs.get('token');
    const url = 'http://localhost:8080/api/user/allMessages';
    final body = json.encode(RoomRequest(roomId: id).toJson());
    final headers = {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    };
    final response =
    await http.post(Uri.parse(url), headers: headers, body: body);

    if (response.statusCode == 200) {
      final List<dynamic> jsonData = json.decode(response.body);
      final List<MessageDTO> messages = jsonData
          .map((json) => MessageDTO.fromJson(json))
          .toList();
      setState(() {
        this.messages = messages;
      });
      print("Success");
      connectToSocketIO(token);
      return messages;
    } else {
      print('Request failed with status: ${response.statusCode}');
      return [];
    }
  }
  void connectToSocketIO(Object? token) {
    //TODO
  }


  @override
  Widget build(BuildContext context) {
    bool isMessageSectionVisible = messages != null;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Chat'),
      ),
      body: Row(
        children: <Widget>[
          Expanded(
            flex: 1,
            child: Container(
              color: Colors.grey[200],
              child: Column(
                children: <Widget>[
                  Expanded(
                    child: Container(
                      margin: const EdgeInsets.all(10),
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        border: Border.all(color: Colors.grey),
                        borderRadius: BorderRadius.circular(8.0),
                        color: Colors.white,
                      ),
                      child: ListView.builder(
                        itemCount: chatRooms.length,
                        itemBuilder: (context, index) {
                          final chatRoom = chatRooms[index];
                          final lastMessage = chatRoom.messages.isNotEmpty
                              ? chatRoom.messages.last
                              : null;

                          return ListTile(
                            title: Text(
                              chatRoom.roomName,
                              style: const TextStyle(
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            subtitle: lastMessage != null
                                ? Text(
                              lastMessage.text,
                              style: const TextStyle(
                                color: Colors.grey,
                              ),
                            )
                                : null,
                            onTap: () {
                              setState(() {
                                joinChatRoom(chatRoom.id);
                                isMessageSectionVisible = true;
                              });
                            },
                          );
                        },
                      ),
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: Row(
                      children: <Widget>[
                        ElevatedButton(
                          onPressed: () {
                            // Handle chat creation
                          },
                          child: const Text('Create Chat'),
                        ),
                        const SizedBox(width: 8),
                        IconButton(
                          icon: const Icon(Icons.exit_to_app),
                          onPressed: () {
                            // Handle logout
                          },
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          const VerticalDivider(width: 1),
          Expanded(
            flex: 3,
            child: Visibility(
              visible: isMessageSectionVisible,
              child: Column(
                children: <Widget>[
                  Container(
                    padding: const EdgeInsets.all(8.0),
                    decoration: const BoxDecoration(
                      border: Border(
                        bottom: BorderSide(
                          color: Colors.grey,
                          width: 1.0,
                        ),
                      ),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: <Widget>[
                        IconButton(
                          icon: const Icon(Icons.people),
                          onPressed: () {
                            // Handle participants button
                          },
                        ),
                        IconButton(
                          icon: const Icon(Icons.person_add),
                          onPressed: () {
                            // Handle invite button
                          },
                        ),
                      ],
                    ),
                  ),
                  Expanded(
                    child: ListView.builder(
                      itemCount: messages != null ? messages!.length : 0,
                      itemBuilder: (context, index) {
                        final message = messages![index];
                        return ListTile(
                          title: Text(message.text),
                          subtitle: Text(message.sender),
                        );
                      },
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.all(8.0),
                    child: Row(
                      children: <Widget>[
                        const Expanded(
                          child: TextField(
                            decoration: InputDecoration(
                              labelText: 'Enter message',
                            ),
                          ),
                        ),
                        IconButton(
                          icon: const Icon(Icons.send),
                          onPressed: () {
                            // Handle message sending
                          },
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}