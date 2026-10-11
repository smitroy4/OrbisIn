package com.smit.orbisIn.userService.service;

import com.smit.orbisIn.userService.dto.LoginRequestDto;
import com.smit.orbisIn.userService.dto.SignupRequestDto;
import com.smit.orbisIn.userService.dto.UserDto;
import com.smit.orbisIn.userService.entity.User;
import com.smit.orbisIn.userService.exception.BadRequestException;
import com.smit.orbisIn.userService.repository.UserRepository;
import com.smit.orbisIn.userService.utils.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final com.smit.orbisIn.userService.producer.UserEventProducer userEventProducer;

    public UserDto signUp(SignupRequestDto signupRequestDto) {
        log.info("Signup a user with email: {}", signupRequestDto.getEmail());

        boolean exists = userRepository.existsByEmail(signupRequestDto.getEmail());
        if(exists) {
            throw new BadRequestException("User already exists");
        }

        User user = modelMapper.map(signupRequestDto, User.class);
        user.setPassword(BCrypt.hash(signupRequestDto.getPassword()));

        user = userRepository.save(user);
        UserDto userDto = modelMapper.map(user, UserDto.class);

        // Publish event (non-blocking for signup success: failures are logged only)
        try {
            userEventProducer.sendUserCreated(
                    new com.smit.orbisIn.userService.event.UserCreatedEvent(
                            user.getId(), user.getName(), user.getEmail()));
        } catch (Exception e) {
            log.error("Signup succeeded but failed to publish UserCreatedEvent for userId: {}", user.getId(), e);
        }

        return userDto;
    }

    public String login(LoginRequestDto loginRequestDto) {
        log.info("Login request for user with email: {}", loginRequestDto.getEmail());

        User user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(() -> new BadRequestException(
                "Incorrect email or password"));

        boolean isPasswordMatch = BCrypt.match(loginRequestDto.getPassword(), user.getPassword());

        if(!isPasswordMatch) {
            throw new BadRequestException("Incorrect email or password");
        }

        return jwtService.generateAccessToken(user);
    }
}
