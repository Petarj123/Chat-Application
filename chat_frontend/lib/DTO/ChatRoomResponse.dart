import 'dart:convert';

import 'package:http/http.dart' as http;

Future<List<ChatRoomDTO>> getAllChats() async {
  const url = 'http://localhost:8080/api/user/allChats';
  final headers = {'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsImlkIjoiNjQ1M2UzZDk4MTMxNjc0YjIzZGVkMDYwIiwic3ViIjoicGphbmtvdmljMDNAZ21haWwuY29tIiwiaWF0IjoxNjg2MzI0NjQ4LCJleHAiOjE2ODYzMjQ2Njh9.w0JoLZuv8SUg-if7IRtBFmayy03fINDAHvqJNba0DKk'};

  final response = await http.get(Uri.parse(url), headers: headers);

  if (response.statusCode == 200) {
    // Request successful
    final List<dynamic> jsonData = json.decode(response.body);
    final List<ChatRoomDTO> chatRooms = jsonData
        .map((json) => ChatRoomDTO.fromJson(json))
        .toList();
    for(var chat in chatRooms){
      print(chat.roomName);
    }
    return chatRooms;
  } else {
    // Request failed
    print('Request failed with status: ${response.statusCode}');
    return [];
  }
}
Future<List<MessageDTO>> getAllMessages() async {
  const url = 'http://localhost:8080/api/user/allMessages';
  final headers = {
    'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsImlkIjoiNjQ1M2UzZDk4MTMxNjc0YjIzZGVkMDYwIiwic3ViIjoicGphbmtvdmljMDNAZ21haWwuY29tIiwiaWF0IjoxNjg2MzI0NjQ4LCJleHAiOjE2ODYzMjQ2Njh9.w0JoLZuv8SUg-if7IRtBFmayy03fINDAHvqJNba0DKk',
    'Content-Type': 'application/json',
  };
  final body = json.encode(RoomRequest(roomId: '6483242e6922286db0d1ecaa').toJson());

  final response = await http.post(Uri.parse(url), headers: headers, body: body);

  if (response.statusCode == 200) {
    // Request successful
    final List<dynamic> jsonData = json.decode(response.body);
    final List<MessageDTO> messages = jsonData
        .map((json) => MessageDTO.fromJson(json))
        .toList();

    return messages;
  } else {
    // Request failed
    print('Request failed with status: ${response.statusCode}');
    return [];
  }
}

class RoomRequest {
  final String roomId;

  RoomRequest({required this.roomId});

  Map<String, dynamic> toJson() {
    return {
      'roomId': roomId,
    };
  }
}
class ChatRoomDTO {
  final String id;
  final String roomName;
  final List<String> participantIds;
  final List<MessageDTO> messages;
  final DateTime createdAt;

  ChatRoomDTO({
    required this.id,
    required this.roomName,
    required this.participantIds,
    required this.messages,
    required this.createdAt,
  });

  factory ChatRoomDTO.fromJson(Map<String, dynamic> json) {
    return ChatRoomDTO(
      id: json['id'],
      roomName: json['roomName'],
      participantIds: List<String>.from(json['participantIds']),
      messages: List<MessageDTO>.from(
        json['messages'].map((messageJson) => MessageDTO.fromJson(messageJson)),
      ),
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}

class MessageDTO {
  final String messageId;
  final String text;
  final String sender;
  final String sentAt;

  MessageDTO({
    required this.messageId,
    required this.text,
    required this.sender,
    required this.sentAt,
  });

  factory MessageDTO.fromJson(Map<String, dynamic> json) {
    return MessageDTO(
      messageId: json['messageId'],
      text: json['text'] as String? ?? '',
      sender: json['sender'] is String ? json['sender'] : json['sender'].toString(),
      sentAt: json['sentAt'].toString(),
    );
  }
}
