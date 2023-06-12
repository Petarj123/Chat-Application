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
  final String createdAt;

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
      createdAt: json['createdAt'].toString(),
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
