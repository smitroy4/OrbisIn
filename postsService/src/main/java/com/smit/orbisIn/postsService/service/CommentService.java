package com.smit.orbisIn.postsService.service;

import com.smit.orbisIn.postsService.context.ContextHolder;
import com.smit.orbisIn.postsService.dto.CommentCreateRequestDto;
import com.smit.orbisIn.postsService.dto.CommentDto;
import com.smit.orbisIn.postsService.entity.Comment;
import com.smit.orbisIn.postsService.exception.BadRequestException;
import com.smit.orbisIn.postsService.exception.ResourceNotFoundException;
import com.smit.orbisIn.postsService.repository.CommentRepository;
import com.smit.orbisIn.postsService.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    private Long currentUserId() {
        Long userId = ContextHolder.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid user context");
        }
        return userId;
    }

    @Transactional
    public CommentDto addComment(Long postId, CommentCreateRequestDto dto) {
        Long userId = currentUserId();
        postRepository.findById(postId).orElseThrow(()
                -> new ResourceNotFoundException("Post not found with ID: " + postId));
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BadRequestException("Comment content must not be empty");
        }
        log.info("User with ID: {} commenting on post with ID: {}", userId, postId);
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent().trim());
        comment = commentRepository.save(comment);
        return modelMapper.map(comment, CommentDto.class);
    }

    public List<CommentDto> getComments(Long postId) {
        postRepository.findById(postId).orElseThrow(()
                -> new ResourceNotFoundException("Post not found with ID: " + postId));
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map((comment) -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Long userId = currentUserId();
        Comment comment = commentRepository.findById(commentId).orElseThrow(()
                -> new ResourceNotFoundException("Comment not found with ID: " + commentId));
        if (!comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }
}
