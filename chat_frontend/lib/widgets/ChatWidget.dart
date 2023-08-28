import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
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
  bool isMessageSectionVisible = false;

  @override
  void initState() {
    super.initState();
    fetchChatRooms();
    connectToSocketIO();
  }

  Future<String> _getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('token') ?? '';
  }

  Future<List<ChatRoomDTO>> getAllChats() async {
    final token = await _getToken();
    const url = 'http://192.168.0.18:8080/api/user/allChats';

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
  Future<List<ChatRoomDTO>> leaveChatRoom() async {
    Completer<List<ChatRoomDTO>> completer = Completer<List<ChatRoomDTO>>();

    socket!.emitWithAck(
      'leaveChatRoom',
      {'roomId': activeRoomId},
      ack: (List<dynamic> data) {
        setState(() {
          chatRooms = data.map((json) => ChatRoomDTO.fromJson(json)).toList();
          completer.complete(chatRooms);
        });
      },
    );

    return completer.future;
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

    final token = await _getToken();
    const url = 'http://192.168.0.18:8080/api/user/allMessages';
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
    final token = await _getToken();
    socket = IO.io(
      'http://192.168.0.18:8000',
      IO.OptionBuilder()
          .setTransports(['websocket'])
          .setQuery({'token': 'Bearer $token'})
          .disableAutoConnect()
          .build(),
    );
    print('USPEH');

    socket?.onConnect((_) {
      print('Connected to Socket.IO server');
    });

    socket?.onDisconnect((_) {
      print('Disconnected from Socket.IO server');
    });
    socket!.on('error', (data) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(data.toString())));
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
    if (roomName.isNotEmpty) {
      socket!.emitWithAck('createChatRoom', {'roomName': roomName},
          ack: (List<dynamic> data) {
            setState(() {
              chatRooms = data.map((json) =>
                  ChatRoomDTO.fromJson(json as Map<String, dynamic>)).toList();
            });
          }
      );
    }
  }

  void acceptInvite(String invitationLink) {
    if (invitationLink.isNotEmpty) {
      socket!.emitWithAck('acceptInvite', {'invitationLink': invitationLink},
          ack: (List<dynamic> data) {
            if (data[0] is String) {
              ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(data[0])));
            } else {
              setState(() {
                chatRooms = data.map((json) =>
                    ChatRoomDTO.fromJson(json as Map<String, dynamic>)).toList();
              });
            }
          }
      );
    }
  }

  void scrollToBottom() {
    SchedulerBinding.instance.addPostFrameCallback((_) {
      _messageScrollController.jumpTo(
        _messageScrollController.position.maxScrollExtent,);
    });
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    prefs.remove('token');

    Navigator.pushReplacementNamed(context, '/login');
  }

  Future<String> generateInvitationLink() {
    Completer<String> completer = Completer<String>();
    socket!.emitWithAck('createInvite', {'roomId': activeRoomId},
        ack: (String data) {
          completer.complete(data);
        }
    );
    return completer.future;
  }

  Future<Map<String, String>> showParticipants() async {
    Completer<Map<String, String>> completer = Completer<Map<String, String>>();
    socket!.emitWithAck(
      'getParticipants',
      {'roomId': activeRoomId},
      ack: (Map<String, dynamic> data) {
        Map<String, String> participants = data.map((key, value) => MapEntry(key.toString(), value.toString()));
        completer.complete(participants);
      },
    );
    return completer.future;
  }
  Future<Map<String, String>> kickUserFromGroup(String userEmail) async {
    Completer<Map<String, String>> completer = Completer<Map<String, String>>();
    socket!.emitWithAck(
      'kick',
      {'roomId': activeRoomId, 'email': userEmail}, // Added 'email' to the data being emitted
      ack: (Map<String, dynamic> data) {
        Map<String, String> participants = data.map((key, value) => MapEntry(key.toString(), value.toString()));
        completer.complete(participants);
      },
    );
    return completer.future;
  }
  Future<Map<String, String>> promoteParticipantToGroupAdmin(String userEmail) async {
    Completer<Map<String, String>> completer = Completer<Map<String, String>>();
    socket!.emitWithAck(
      'promote',
      {'roomId': activeRoomId, 'email': userEmail}, // Added 'email' to the data being emitted
      ack: (Map<String, dynamic> data) {
        Map<String, String> participants = data.map((key, value) => MapEntry(key.toString(), value.toString()));
        completer.complete(participants);
      },
    );
    return completer.future;
  }
  Future<Map<String, String>> demoteGroupAdminToParticipant(String userEmail) async {
    Completer<Map<String, String>> completer = Completer<Map<String, String>>();
    socket!.emitWithAck(
      'demote',
      {'roomId': activeRoomId, 'email': userEmail}, // Added 'email' to the data being emitted
      ack: (Map<String, dynamic> data) {
        Map<String, String> participants = data.map((key, value) => MapEntry(key.toString(), value.toString()));
        completer.complete(participants);
      },
    );
    return completer.future;
  }
  Future<Map<String, String>> getEmailAndRole() async {
    Completer<Map<String, String>> completer = Completer<Map<String, String>>();
    socket!.emitWithAck(
      'getRole',
      {'roomId': activeRoomId},
        ack: (Map<String, dynamic> data) {
          Map<String, String> user = data.map((key, value) => MapEntry(key.toString(), value.toString()));
          completer.complete(user);
        },
    );
    return completer.future;
  }
  Widget _roleIcon(String role) {
    IconData iconData;
    String tooltipMessage;

    switch (role) {
      case "GROUP CREATOR":
        iconData = Icons.person_pin;
        tooltipMessage = "Group Creator";
        break;
      case "GROUP ADMIN":
        iconData = Icons.admin_panel_settings;
        tooltipMessage = "Group Admin";
        break;
      default: // PARTICIPANT
        iconData = Icons.person;
        tooltipMessage = "Participant";
    }

    return Tooltip(
      message: tooltipMessage,
      child: Icon(iconData),
    );
  }




  @override
  Widget build(BuildContext context) {
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

                          return Card(
                            elevation: 5.0,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10.0),
                            ),
                            child: ListTile(
                              title: Text(
                                chatRoom.roomName,
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: Colors.blueAccent,
                                ),
                              ),
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
                            Map<String, String> currentUserEmailAndRole = await getEmailAndRole();
                            Map<String, String> participants = await showParticipants();

                            String currentUserEmail = currentUserEmailAndRole.keys.first;
                            String currentUserRole = currentUserEmailAndRole.values.first;

                            showDialog(
                              context: context,
                              builder: (BuildContext context) {
                                return AlertDialog(
                                  title: const Text('Participants'),
                                  content: ConstrainedBox(
                                    constraints: const BoxConstraints(maxHeight: 300),
                                    child: SingleChildScrollView(
                                      child: Column(
                                        children: participants.entries.map((entry) =>
                                            ListTile(
                                              leading: _roleIcon(entry.value),
                                              title: Text("${entry.key}"),
                                              trailing:
                                              (currentUserRole == "GROUP CREATOR" && entry.key != currentUserEmail) ||
                                                  (currentUserRole == "GROUP ADMIN" && entry.value == "PARTICIPANT" && entry.key != currentUserEmail) ?
                                              Row(
                                                mainAxisSize: MainAxisSize.min,
                                                children: [
                                                  IconButton(
                                                    icon: const Icon(Icons.keyboard_arrow_up),
                                                    tooltip: "Promote",
                                                    onPressed: () async {
                                                      await promoteParticipantToGroupAdmin(entry.key);
                                                      Navigator.of(context).pop();
                                                    },
                                                  ),
                                                  IconButton(
                                                    icon: const Icon(Icons.keyboard_arrow_down),
                                                    tooltip: "Demote",
                                                    onPressed: () async{
                                                      await demoteGroupAdminToParticipant(entry.key);
                                                      Navigator.of(context).pop();
                                                    },
                                                  ),
                                                  IconButton(
                                                    icon: const Icon(Icons.remove_circle),
                                                    tooltip: "Kick Out",
                                                    onPressed: () async {
                                                      await kickUserFromGroup(entry.key);
                                                      Navigator.of(context).pop();
                                                    },
                                                  ),
                                                ],
                                              )
                                                  : null,
                                            )
                                        ).toList(),
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
                                builder: (BuildContext context) {
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
                        IconButton(
                          icon: const Icon(Icons.exit_to_app),
                            onPressed: () async {
                              var updatedChatRooms = await leaveChatRoom();
                              setState(() {
                                chatRooms = updatedChatRooms;
                                isMessageSectionVisible = false;
                              });
                            }
                        )
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
                    padding: const EdgeInsets.symmetric(
                        horizontal: 8.0, vertical: 10.0),
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
                            backgroundColor: MaterialStateProperty.all<Color>(
                                Colors.blue),
                            shape: MaterialStateProperty.all<
                                RoundedRectangleBorder>(
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
                        const SizedBox(width: 10), // for spacing
                        IconButton(
                          icon: const Icon(Icons.mic, color: Colors.blue),
                          onPressed: () {
                            showDialog(
                              context: context,
                              builder: (BuildContext context) {
                                return StatefulBuilder(
                                  builder: (BuildContext context, StateSetter setState) {
                                    ValueNotifier<int> recordingDuration = ValueNotifier<int>(0);
                                    Timer? timer;

                                    void startRecording() {
                                      timer = Timer.periodic(Duration(seconds: 1), (timer) {
                                        recordingDuration.value++;
                                      });
                                    }

                                    void stopRecording() {
                                      timer?.cancel();
                                    }

                                    return AlertDialog(
                                      title: const Text('Recording'),
                                      content: Column(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          ValueListenableBuilder<int>(
                                            valueListenable: recordingDuration,
                                            builder: (context, value, child) {
                                              return Text('Recording duration: ${value}s');
                                            },
                                          ),
                                          Slider(
                                            value: recordingDuration.value.toDouble(),
                                            onChanged: (double value) {},
                                            min: 0,
                                            max: 60,
                                          ),
                                          Row(
                                            mainAxisAlignment: MainAxisAlignment.center,
                                            children: [
                                              ValueListenableBuilder<int>(
                                                valueListenable: recordingDuration,
                                                builder: (context, value, child) {
                                                  return ElevatedButton(
                                                    onPressed: timer?.isActive ?? false
                                                        ? stopRecording
                                                        : startRecording,
                                                    child: timer?.isActive ?? false
                                                        ? const Text('Stop')
                                                        : const Text('Start'),
                                                  );
                                                },
                                              ),
                                            ],
                                          ),
                                          const SizedBox(height: 20),
                                          Row(
                                            mainAxisAlignment: MainAxisAlignment.center,
                                            children: [
                                              ElevatedButton(
                                                child: const Text('Send'),
                                                onPressed: () {},
                                              ),
                                              const SizedBox(width: 10),
                                              ElevatedButton(
                                                child: const Text('Cancel'),
                                                onPressed: () {
                                                  Navigator.of(context).pop();
                                                },
                                              ),
                                            ],
                                          ),
                                        ],
                                      ),
                                    );
                                  },
                                );
                              },
                            );
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
