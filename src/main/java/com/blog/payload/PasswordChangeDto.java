package com.blog.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeDto {
    private String oldPassword;
    private String newPassword;
}
