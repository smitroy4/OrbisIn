package com.smit.orbisIn.postsService.service;

import com.smit.orbisIn.postsService.context.ContextHolder;
import com.smit.orbisIn.postsService.entity.PostLike;
import com.smit.orbisIn.postsService.exception.BadRequestException;
import com.smit.orbisIn.postsService.exception.ResourceNotFoundException;
import com.smit.orbisIn.postsService.repository.PostLikeRepository;
import com.smit.orbisIn.postsService.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final com.smit.orbisIn.postsService.producer.PostEventProducer postEventProducer;

    @Transactional
    public void likePost(Long postId) {
        Long userId = ContextHolder.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid user context");
        }
        log.info("User with ID: {} liking the post with ID: {}", userId, postId);

        com.smit.orbisIn.postsService.entity.Post post = postRepository.findById(postId).orElseThrow(()
                -> new ResourceNotFoundException("Post not found with ID: "+postId));

        boolean hasAlreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(hasAlreadyLiked) throw new BadRequestException("You cannot like the post again");

        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLikeRepository.save(postLike);

        try {
            postEventProducer.sendPostLiked(
                    new com.smit.orbisIn.postsService.event.PostLikedEvent(
                            postId, userId, post.getUserId()));
        } catch (Exception e) {
            log.error("Post liked but failed to publish PostLikedEvent for postId: {}", postId, e);
        }

    }


    @Transactional
    public void unlikePost(Long postId) {
        Long userId = ContextHolder.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid user context");
        }
        log.info("User with ID: {} unliking the post with ID: {}", userId, postId);

        postRepository.findById(postId).orElseThrow(()
                -> new ResourceNotFoundException("Post not found with ID: "+postId));

        boolean hasAlreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(!hasAlreadyLiked) throw new BadRequestException("You cannot unlike the post that you have not liked yet");

        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
    }
}
