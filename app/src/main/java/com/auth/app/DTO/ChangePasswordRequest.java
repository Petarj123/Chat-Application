package com.auth.app.DTO;

public record ChangePasswordRequest (String oldPassword, String newPassword){
}
