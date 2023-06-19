package com.auth.app.DTO;

import org.bson.types.Binary;

public record VoiceMessageRequest(String roomId, Binary voiceMessage) {
}
