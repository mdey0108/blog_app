package com.blog.controller;

import com.blog.entity.Role;
import com.blog.entity.User;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.RoleRepository;
import com.blog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private UserRepository userRepository;
    private RoleRepository roleRepository;

    public UserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<Void> makeAdmin(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_ADMIN"));

        user.getRoles().add(adminRole);
        userRepository.save(user);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role/revoke")
    public ResponseEntity<Void> revokeAdmin(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_ADMIN"));

        user.getRoles().remove(adminRole);
        userRepository.save(user);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.updateStatus(id, true);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Soft delete: Deactivate user
        userRepository.updateStatus(id, false);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        // return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<com.blog.payload.UserProfileDto> getCurrentUserProfile() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        com.blog.payload.UserProfileDto profile = new com.blog.payload.UserProfileDto();
        profile.setId(user.getId());
        profile.setName(user.getName());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());

        return ResponseEntity.ok(profile);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ResponseEntity<com.blog.payload.UserProfileDto> updateUserProfile(
            @RequestBody com.blog.payload.UserProfileDto userDto) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        user.setName(userDto.getName());

        // Update Username if changed and unique
        if (userDto.getUsername() != null && !userDto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(userDto.getUsername())) {
                throw new com.blog.exception.BlogAPIException(HttpStatus.BAD_REQUEST, "Username is already taken!");
            }
            user.setUsername(userDto.getUsername());
        }

        // Update Email if changed and unique
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new com.blog.exception.BlogAPIException(HttpStatus.BAD_REQUEST, "Email is already taken!");
            }
            user.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(user);

        com.blog.payload.UserProfileDto profile = new com.blog.payload.UserProfileDto();
        profile.setId(user.getId());
        profile.setName(user.getName());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setAdmin(user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")));

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<com.blog.payload.UserProfileDto> getUserProfileById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        com.blog.payload.UserProfileDto profile = new com.blog.payload.UserProfileDto();
        profile.setId(user.getId());
        profile.setName(user.getName());
        profile.setUsername(user.getUsername());
        // We might choose NOT to expose email publicly, but user asked for "view
        // profile"
        // usually email is private. However, let's include it for consistency with
        // "user-profile".
        // Or if privacy is concern, we can omit email for public view.
        // For now, I'll restrict email to only own profile, so no email here.
        profile.setUsername(user.getUsername());

        return ResponseEntity.ok(profile);
    }
}
