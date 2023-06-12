import 'dart:async';
import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:http/http.dart' as http;
import '../DTO/ChatRoomResponse.dart';
import 'package:socket_io_client/socket_io_client.dart' as IO;

class ChatWidget extends StatefulWidget {
  const ChatWidget({super.key});

  @override
  _ChatWidgetState createState() => _ChatWidgetState();
}

class _ChatWidgetState extends State<ChatWidget> {
  List<ChatRoomDTO> chatRooms = [];
  List<MessageDTO>? messages;
  String? activeRoomId;
  final textEditingController = TextEditingController();
  IO.Socket? socket;
  final ScrollController _messageScrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    fetchChatRooms();
    connectToSocketIO();
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
    if (activeRoomId != null) {
      socket!.emit('leaveRoom', {'roomId': activeRoomId});
    }

    activeRoomId = id;

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
      socket!.emit('joinRoom', {'roomId': activeRoomId});
      scrollToBottom();
      return messages;
    } else {
      print('Request failed with status: ${response.statusCode}');
      return [];
    }
  }

  Future<void> connectToSocketIO() async {
    final prefs = await SharedPreferences.getInstance();
    Object? token = prefs.get('token');
    print('Token $token');
    socket = IO.io(
      'http://127.0.0.1:8000',
      IO.OptionBuilder()
          .setTransports(['websocket'])
          .setQuery({'token': 'Bearer $token'})
          .disableAutoConnect()
          .build(),
    );

    socket?.onConnect((_) {
      print('Connected to Socket.IO server');
    });

    socket?.onDisconnect((_) {
      print('Disconnected from Socket.IO server');
    });

    socket?.on('newMessage', (data) {
      final message = MessageDTO.fromJson(data as Map<String, dynamic>);

      setState(() {
        messages?.add(message);
      });

      scrollToBottom();
    });

    socket?.connect();
  }

  void sendMessage() {
    if (activeRoomId != null && textEditingController.text.isNotEmpty) {
      final message = {
        'roomId': activeRoomId,
        'text': textEditingController.text,
      };

      socket!.emit('sendMessage', message);

      textEditingController.clear();
    }
  }

  void createChatRoom(String roomName) {
    if(roomName.isNotEmpty) {
      socket!.emitWithAck('createChatRoom', {'roomName': roomName},
          ack: (List<dynamic> data) {
            setState(() {
              chatRooms = data.map((json) => ChatRoomDTO.fromJson(json as Map<String, dynamic>)).toList();
            });
          }
      );
    }
  }
  void acceptInvite(String invitationLink){
    if (invitationLink.isNotEmpty) {
      socket!.emitWithAck('acceptInvite', {'invitationLink': invitationLink},
          ack: (List<dynamic> data) {
            setState(() {
              chatRooms = data.map((json) =>
                  ChatRoomDTO.fromJson(json as Map<String, dynamic>)).toList();
            });
          }
      );
    }
  }
  void scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _messageScrollController.jumpTo(
        _messageScrollController.position.maxScrollExtent,
      );
    });
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    prefs.remove('token');

    Navigator.pushReplacementNamed(context, '/login');
  }
  Future<String> generateInvitationLink(){
    Completer<String> completer = Completer<String>();
    socket!.emitWithAck('createInvite', {'roomId' : activeRoomId},
        ack: (String data) {
          completer.complete(data);
        }
    );
    return completer.future;
  }
  Future<List<String>> showParticipants() async {
    Completer<List<String>> completer = Completer<List<String>>();
    socket!.emitWithAck(
      'getParticipants',
      {'roomId': activeRoomId},
      ack: (List<dynamic> data) {
        print(data);
        List<String> participants = List<String>.from(data);
        completer.complete(participants);
      },
    );
    return completer.future;
  }


  @override
  Widget build(BuildContext context) {
    bool isMessageSectionVisible = messages != null;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Chat'),
        backgroundColor: Colors.blueAccent,
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

                          return Card(
                            elevation: 5.0,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10.0),
                            ),
                            child: ListTile(
                              title: Text(
                                chatRoom.roomName,
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: Colors.blueAccent,
                                ),
                              ),
                              subtitle: lastMessage != null
                                  ? Text(
                                lastMessage.text,
                                style: TextStyle(
                                  color: Colors.grey,
                                ),
                              )
                                  : null,
                              onTap: () {
                                setState(() {
                                  joinChatRoom(chatRoom.id);
                                  isMessageSectionVisible = true;
                                });
                                scrollToBottom();
                              },
                            ),
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
                            showDialog(
                              context: context,
                              builder: (context) {
                                TextEditingController chatRoomController = TextEditingController();
                                return AlertDialog(
                                  title: const Text('Create Chat'),
                                  content: TextField(
                                    controller: chatRoomController,
                                    decoration: const InputDecoration(
                                      hintText: 'Enter Chat Room Name',
                                    ),
                                  ),
                                  actions: <Widget>[
                                    TextButton(
                                      child: const Text('Create'),
                                      onPressed: () {
                                        createChatRoom(chatRoomController.text);
                                        Navigator.of(context).pop();
                                      },
                                    ),
                                  ],
                                );
                              },
                            );
                          },
                          child: const Text('Create Chat'),
                        ),
                        const SizedBox(width: 16),
                        ElevatedButton(
                          onPressed: () {
                            showDialog(
                              context: context,
                              builder: (context) {
                                TextEditingController joinChatRoomController = TextEditingController();
                                return AlertDialog(
                                  title: const Text('Join Chat'),
                                  content: TextField(
                                    controller: joinChatRoomController,
                                    decoration: const InputDecoration(
                                      hintText: 'Enter Invitation Link',
                                    ),
                                  ),
                                  actions: <Widget>[
                                    TextButton(
                                      child: const Text('Join'),
                                      onPressed: () {
                                        acceptInvite(joinChatRoomController.text);
                                        Navigator.of(context).pop();
                                      },
                                    ),
                                  ],
                                );
                              },
                            );
                          },
                          child: const Text('Join Chat'),
                        ),
                        const SizedBox(width: 8),
                        IconButton(
                          icon: const Icon(Icons.exit_to_app),
                          onPressed: () {
                            logout();
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
                          onPressed: () async {
                            List<String> participants = await showParticipants();
                            showDialog(
                              context: context,
                              builder: (BuildContext context) {
                                return AlertDialog(
                                  title: const Text('Participants'),
                                  content: ConstrainedBox(
                                    constraints: BoxConstraints(maxHeight: 300),
                                    child: SingleChildScrollView(
                                      child: Column(
                                        children: participants.map((participant) => ListTile(title: Text(participant))).toList(),
                                      ),
                                    ),
                                  ),
                                  actions: <Widget>[
                                    TextButton(
                                      child: const Text('Close'),
                                      onPressed: () {
                                        Navigator.of(context).pop();
                                      },
                                    ),
                                  ],
                                );
                              },
                            );
                          },
                        ),
                        IconButton(
                          icon: const Icon(Icons.person_add),
                          onPressed: () async {
                            String invitationLink = await generateInvitationLink();
                            showDialog(
                                context: context,
                                builder: (BuildContext context){
                                  return AlertDialog(
                                    title: const Text('Invitation Link'),
                                    content: SelectableText(invitationLink),
                                    actions: <Widget>[
                                      TextButton(
                                        child: const Text('OK'),
                                        onPressed: () {
                                          Navigator.of(context).pop();
                                        },
                                      )
                                    ],
                                  );
                                }
                            );
                          },
                        ),
                      ],
                    ),
                  ),
                  Expanded(
                    child: ListView.builder(
                      controller: _messageScrollController,
                      itemCount: messages != null ? messages!.length : 0,
                      itemBuilder: (context, index) {
                        final message = messages![index];
                        return Card(
                          elevation: 2.0,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(10.0),
                          ),
                          child: ListTile(
                            title: Text(
                              message.sender,
                              style: const TextStyle(
                                color: Colors.grey,
                              ),
                            ),
                            subtitle: Text(
                              message.text,
                              style: const TextStyle(
                                color: Colors.black,
                              ),
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                  Container(
                    decoration: BoxDecoration(
                      color: Colors.grey[200],
                      boxShadow: const [
                        BoxShadow(
                          color: Colors.black12,
                          blurRadius: 5, // shadow effect
                        ),
                      ],
                    ),
                    padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 10.0),
                    child: Row(
                      children: <Widget>[
                        Expanded(
                          child: TextField(
                            controller: textEditingController,
                            decoration: InputDecoration(
                              filled: true,
                              fillColor: Colors.white,
                              labelText: 'Enter message',
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(10),
                                borderSide: BorderSide.none,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 10), // for spacing
                        ElevatedButton(
                          style: ButtonStyle(
                            backgroundColor: MaterialStateProperty.all<Color>(Colors.blue),
                            shape: MaterialStateProperty.all<RoundedRectangleBorder>(
                              RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(10),
                              ),
                            ),
                          ),
                          child: const Icon(Icons.send, color: Colors.white),
                          onPressed: () {
                            sendMessage();
                            scrollToBottom();
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

