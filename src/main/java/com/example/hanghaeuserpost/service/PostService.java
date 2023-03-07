package com.example.hanghaeuserpost.service;

import com.example.hanghaeuserpost.dto.PostRequestDto;
import com.example.hanghaeuserpost.dto.PostResponseDto;
import com.example.hanghaeuserpost.entity.Post;
import com.example.hanghaeuserpost.entity.User;
import com.example.hanghaeuserpost.jwt.JwtUtil;
import com.example.hanghaeuserpost.repository.PostRepository;
import com.example.hanghaeuserpost.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    @Transactional
    public PostResponseDto createPost(PostRequestDto requestDto, HttpServletRequest request) {
        User user = getUser(request); // 토큰이 있는 경우 사용자의 정보를 받아온다.
        Post post = postRepository.saveAndFlush(new Post(requestDto, user));
        return new PostResponseDto(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getPost() {
        List<Post> postList = postRepository.findAllByOrderByCreatedAtDesc();

        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        for (Post post : postList) {
            postResponseDtoList.add(new PostResponseDto(post));
        }

        return postResponseDtoList;
    }

    @Transactional
    public PostResponseDto getOnePost(Long id) {
        Post post = getPost(id);    // 게시글이 존재하는지 확인 후 가져온다
        return new PostResponseDto(post);
    }

    @Transactional
    public ResponseEntity update(Long id, PostRequestDto postRequestDto, HttpServletRequest request) {
        User user = getUser(request);   // 토큰이 있는 경우 사용자의 정보를 받아온다.

        Post post = getPost(id);    // 게시글이 존재하는지 확인 후 가져온다

        getPostRole(id, user);  // 권한을 확인한다 (자신이 쓴 글인지 확인)

        post.update(postRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body("수정 완료");
    }

    @Transactional
    public ResponseEntity delete(Long id, HttpServletRequest request) {
        User user = getUser(request);      // 토큰이 있는 경우 사용자의 정보를 받아온다.

        getPost(id);        // 게시글이 존재하는지 확인 후 가져온다

        getPostRole(id, user);  // 권한을 확인한다 (자신이 쓴 글인지 확인)

        postRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("삭제 완료");
    }

    public User getUser(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        Claims claims;

        if (token == null) return null;

        // 토큰이 있는 경우에만 가능
        if (jwtUtil.validateToken(token)) {
            // 토큰에서 사용자 정보 가져오기
            claims = jwtUtil.getUserInfoFromToken(token);
        } else {
            throw new IllegalArgumentException("Token Error");
        }

        // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
        User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
        );
        return user;
    }

    public Post getPost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("게시글이 존재하지 않습니다.")
        );
        return post;
    }

    public void getPostRole(Long id, User user) {
        postRepository.findByIdAndUser(id, user).orElseThrow(
                () -> new IllegalArgumentException("권한이 없습니다.")
        );
    }
}