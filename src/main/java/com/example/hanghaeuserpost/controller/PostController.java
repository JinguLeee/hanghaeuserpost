package com.example.hanghaeuserpost.controller;

import com.example.hanghaeuserpost.dto.PostRequestDto;
import com.example.hanghaeuserpost.dto.PostResponseDto;
import com.example.hanghaeuserpost.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/api/post")
    public PostResponseDto createPost(@RequestBody PostRequestDto postRequestDto, HttpServletRequest request) {
        return postService.createPost(postRequestDto, request);
    }

    @GetMapping("/api/post")
    public List<PostResponseDto> getPosts() {
        return postService.getPost();
    }

    @GetMapping("/api/post/{id}")
    public PostResponseDto getDetail(@PathVariable Long id) {
        return postService.getOnePost(id);
    }

    @PutMapping("/api/post/{id}")
    public ResponseEntity updatePost (@PathVariable Long id, @RequestBody PostRequestDto postRequestDto, HttpServletRequest request) {
        return postService.update(id, postRequestDto, request);
    }

    @DeleteMapping("/api/post/{id}")
    public ResponseEntity deletePost(@PathVariable Long id, HttpServletRequest request) {
        return postService.delete(id, request);
    }
}
