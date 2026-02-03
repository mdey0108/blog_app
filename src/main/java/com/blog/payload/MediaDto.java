package com.blog.payload;

import lombok.Data;

@Data
public class MediaDto {
    private Long id;
    private String fileUrl;
    private String contentType;
}
