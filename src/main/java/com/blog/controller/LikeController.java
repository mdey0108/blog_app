package com.blog.controller;

import com.blog.entity.Comment;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@org.springframework.transaction.annotation.Transactional
public class LikeController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public LikeController(PostRepository postRepository, CommentRepository commentRepository,
            UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long postId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (post.getLikes().contains(user)) {
            post.getLikes().remove(user);
        } else {
            post.getLikes().add(user);
        }
        postRepository.save(post);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Long commentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (comment.getLikes().contains(user)) {
            comment.getLikes().remove(user);
        } else {
            comment.getLikes().add(user);
        }
        commentRepository.save(comment);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
