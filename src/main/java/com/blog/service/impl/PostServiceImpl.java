package com.blog.service.impl;

import com.blog.entity.Category;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.exception.ResourceNotFoundException;
import com.blog.payload.PostDto;
import com.blog.payload.PostResponse;
import com.blog.repository.CategoryRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.UserRepository;
import com.blog.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.blog.repository.MediaRepository;
import com.blog.service.FileService;

@Service
@org.springframework.transaction.annotation.Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final FileService fileService;

    public PostServiceImpl(PostRepository postRepository, CategoryRepository categoryRepository,
            UserRepository userRepository, MediaRepository mediaRepository,
            FileService fileService) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.mediaRepository = mediaRepository;
        this.fileService = fileService;
    }

    @Override
    public PostDto createPost(PostDto postDto, org.springframework.web.multipart.MultipartFile[] files) {
        Category category = categoryRepository.findById(postDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", postDto.getCategoryId()));

        // Get Current User
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Post post = mapToEntity(postDto);
        post.setCategory(category);
        post.setUser(user);

        // Save Post first to get ID
        Post savedPost = postRepository.save(post);

        // Process Files
        if (files != null && files.length > 0) {
            java.util.Set<com.blog.entity.Media> mediaSet = new java.util.HashSet<>();
            for (org.springframework.web.multipart.MultipartFile file : files) {
                try {
                    String filename = fileService.uploadFile(file);
                    com.blog.entity.Media media = new com.blog.entity.Media();
                    // Store full URL basically. Assuming hosted at /uploads/
                    media.setFileUrl("/uploads/" + filename);
                    media.setContentType(file.getContentType());
                    media.setPost(savedPost);
                    mediaSet.add(media);
                    // We can save media indepedently or cascade.
                    // Since it's bidirectional and we used mappedBy, we should add to post and save
                    // post?
                    // OR save media directly since we set the post.
                    mediaRepository.save(media);
                } catch (java.io.IOException e) {
                    throw new RuntimeException("Error occurred while uploading file: " + e.getMessage());
                }
            }
            // If we want the response to include the media immediately without re-fetching
            savedPost.setMedia(mediaSet);
        }

        return mapToDTO(savedPost);
    }

    @Override
    public PostResponse getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Post> posts = postRepository.findAll(pageable);

        List<Post> listOfPosts = posts.getContent();

        List<PostDto> content = listOfPosts.stream().map(post -> mapToDTO(post)).collect(Collectors.toList());

        PostResponse postResponse = new PostResponse();
        postResponse.setContent(content);
        postResponse.setPageNo(posts.getNumber());
        postResponse.setPageSize(posts.getSize());
        postResponse.setTotalElements(posts.getTotalElements());
        postResponse.setTotalPages(posts.getTotalPages());
        postResponse.setLast(posts.isLast());

        return postResponse;
    }

    @Override
    public PostDto getPostById(long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return mapToDTO(post);
    }

    @Override
    public PostDto updatePost(PostDto postDto, long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        Category category = categoryRepository.findById(postDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", postDto.getCategoryId()));

        post.setTitle(postDto.getTitle());
        post.setDescription(postDto.getDescription());
        post.setContent(postDto.getContent());
        post.setCategory(category);

        Post updatedPost = postRepository.save(post);
        return mapToDTO(updatedPost);
    }

    @Override
    public void deletePostById(long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        // Authorization Check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isOwner = post.getUser() != null && post.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new com.blog.exception.BlogAPIException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "You are not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    @Override
    public List<PostDto> getPostsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        List<Post> posts = postRepository.findByCategoryId(categoryId);

        return posts.stream().map((post) -> mapToDTO(post)).collect(Collectors.toList());
    }

    @Override
    public List<PostDto> getPostsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Post> posts = postRepository.findByUserIdOrderByCreatedDateDesc(userId);

        return posts.stream().map((post) -> mapToDTO(post)).collect(Collectors.toList());
    }

    // convert Entity into DTO
    private PostDto mapToDTO(Post post) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        postDto.setDescription(post.getDescription());
        postDto.setContent(post.getContent());
        postDto.setCreatedDate(post.getCreatedDate());
        postDto.setUpdatedDate(post.getUpdatedDate());
        if (post.getCategory() != null) {
            postDto.setCategoryId(post.getCategory().getId());
            postDto.setCategoryName(post.getCategory().getName());
        }
        if (post.getUser() != null) {
            postDto.setAuthorName(post.getUser().getUsername());
            postDto.setUserId(post.getUser().getId());
        }

        // Like Info
        if (post.getLikes() != null) {
            postDto.setLikeCount(post.getLikes().size());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                String username = auth.getName();
                boolean isLiked = post.getLikes().stream()
                        .anyMatch(u -> u.getUsername().equals(username) || u.getEmail().equals(username));
                postDto.setLiked(isLiked);
            }
        }

        // Admin & Active Check
        if (post.getUser() != null) {
            boolean isAdmin = post.getUser().getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
            postDto.setAuthorIsAdmin(isAdmin);
            postDto.setAuthorIsActive(post.getUser().isActive());
        }

        if (post.getMedia() != null) {
            postDto.setMedia(post.getMedia().stream().map(media -> {
                com.blog.payload.MediaDto mediaDto = new com.blog.payload.MediaDto();
                mediaDto.setId(media.getId());
                mediaDto.setFileUrl(media.getFileUrl());
                mediaDto.setContentType(media.getContentType());
                return mediaDto;
            }).collect(Collectors.toSet()));
        }

        return postDto;
    }

    // convert DTO to entity
    private Post mapToEntity(PostDto postDto) {
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setDescription(postDto.getDescription());
        post.setContent(postDto.getContent());
        return post;
    }
}
