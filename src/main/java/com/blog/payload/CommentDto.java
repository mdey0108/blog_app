package com.blog.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private String name;
    private String email;

    @NotEmpty
    @Size(min = 2, message = "Comment body must be minimum 2 characters")
    private String body;

    private Long userId; // To check ownership on frontend
    // private int likeCount;
    private int likeCount;
    private boolean isLiked;
    // private boolean isLiked;
    private boolean userIsAdmin;
    private boolean userIsActive;
}
