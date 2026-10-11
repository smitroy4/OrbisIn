package com.smit.orbisIn.postsService.service;

import com.smit.orbisIn.postsService.dto.PostCreateRequestDto;
import com.smit.orbisIn.postsService.dto.PostDto;
import com.smit.orbisIn.postsService.entity.Post;
import com.smit.orbisIn.postsService.exception.ResourceNotFoundException;
import com.smit.orbisIn.postsService.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final com.smit.orbisIn.postsService.producer.PostEventProducer postEventProducer;
    private final com.smit.orbisIn.postsService.feign.UploaderClient uploaderClient;

    public PostDto createPost(PostCreateRequestDto postCreateRequestDto, Long userId) {
        log.info("Creating post for user with id: {}", userId);
        Post post = modelMapper.map(postCreateRequestDto, Post.class);
        post.setUserId(userId);
        post = postRepository.save(post);
        PostDto postDto = modelMapper.map(post, PostDto.class);
        postDto.setImageUrl(resolveImageUrl(post.getImageUrl()));

        try {
            postEventProducer.sendPostCreated(
                    new com.smit.orbisIn.postsService.event.PostCreatedEvent(
                            post.getId(), post.getUserId(), post.getContent()));
        } catch (Exception e) {
            log.error("Post created but failed to publish PostCreatedEvent for postId: {}", post.getId(), e);
        }

        return postDto;
    }

    public PostDto getPostById(Long postId) {
        log.info("Getting the post with ID: {}", postId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found " +
                "with ID: "+postId));
        PostDto postDto = modelMapper.map(post, PostDto.class);
        postDto.setImageUrl(resolveImageUrl(postDto.getImageUrl()));
        return postDto;
    }

    public List<PostDto> getAllPostsOfUser(Long userId) {
        log.info("Getting all the posts of a user with ID: {}", userId);
        List<Post> postList = postRepository.findByUserId(userId);

        return postList
                .stream()
                .map((element) -> {
                    PostDto dto = modelMapper.map(element, PostDto.class);
                    dto.setImageUrl(resolveImageUrl(dto.getImageUrl()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Posts store the storage {@code key} (not a URL), because presigned URLs
     * expire. Keys are resolved to a fresh URL on every read. Values that are
     * already full URLs (stored before this change) are served as-is.
     */
    private String resolveImageUrl(String stored) {
        if (stored == null || stored.startsWith("http")) {
            return stored;
        }
        try {
            java.util.Map<String, String> res = uploaderClient.presignDownload(stored);
            if (res != null && res.get("url") != null) {
                return res.get("url");
            }
        } catch (Exception e) {
            log.warn("Could not resolve presigned URL for image key: {}", stored);
        }
        return stored;
    }
}
