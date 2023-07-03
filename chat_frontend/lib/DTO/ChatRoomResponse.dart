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
  final List<String> groupAdmins;
  final List<String> participantIds;
  final List<MessageDTO> messages;
  final String createdAt;
  final String createdBy;
  ChatRoomDTO({
    required this.id,
    required this.roomName,
    required this.groupAdmins,
    required this.participantIds,
    required this.messages,
    required this.createdAt,
    required this.createdBy,
  });

  factory ChatRoomDTO.fromJson(Map<String, dynamic> json) {
    return ChatRoomDTO(
      id: json['id'],
      roomName: json['roomName'],
      groupAdmins: List<String>.from(json['groupAdmins']),
      participantIds: List<String>.from(json['participantIds']),
      messages: List<MessageDTO>.from(
        json['messages'].map((messageJson) => MessageDTO.fromJson(messageJson)),
      ),
      createdAt: json['createdAt'].toString(),
      createdBy: json['createdBy'],
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
