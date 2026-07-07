package com.smit.orbisIn.postsService.service;

import com.smit.orbisIn.postsService.entity.PostLike;
import com.smit.orbisIn.postsService.exception.BadRequestException;
import com.smit.orbisIn.postsService.exception.ResourceNotFoundException;
import com.smit.orbisIn.postsService.repository.PostLikeRepository;
import com.smit.orbisIn.postsService.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public void likePost(Long postId) {
        Long userId = 1L;
        log.info("User with ID: {} liking the post with ID: {}", userId, postId);

        postRepository.findById(postId).orElseThrow(()
                -> new ResourceNotFoundException("Post not found with ID: "+postId));

        boolean hasAlreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(hasAlreadyLiked) throw new BadRequestException("You cannot like the post again");

        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLikeRepository.save(postLike);

//        TODO: send notification to the owner of the post

    }


    @Transactional
    public void unlikePost(Long postId) {
        Long userId = 1L;
        log.info("User with ID: {} unliking the post with ID: {}", userId, postId);

        postRepository.findById(postId).orElseThrow(()
                -> new ResourceNotFoundException("Post not found with ID: "+postId));

        boolean hasAlreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(!hasAlreadyLiked) throw new BadRequestException("You cannot unlike the post that you have not liked yet");

        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
    }
}
