package com.blog.payload;

import lombok.Data;

@Data
public class UserProfileDto {
    private Long id;
    private String name;
    private String username;
    // private String username;
    private String email;
    private boolean isAdmin;
}
