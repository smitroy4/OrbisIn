package com.smit.orbisIn.postsService.controller;

import com.smit.orbisIn.postsService.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/likes")
@RequiredArgsConstructor
@RestController
public class PostLikesController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}")
    public ResponseEntity<Void> likePost(@PathVariable Long postId) {
        postLikeService.likePost(postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId) {
        postLikeService.unlikePost(postId);
        return ResponseEntity.noContent().build();
    }
}
