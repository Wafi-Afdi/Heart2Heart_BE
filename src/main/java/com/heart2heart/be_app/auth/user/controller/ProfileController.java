package com.heart2heart.be_app.auth.user.controller;

import com.heart2heart.be_app.auth.user.dto.UserProfileResponse;
import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.auth.user.repository.UserRepository;
import com.heart2heart.be_app.auth.user.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class ProfileController {
    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private JWTService jwtService;

    @Autowired
    public ProfileController(UserRepository userRepository, AuthenticationManager authenticationManager, JWTService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        UserProfileResponse profileResponse = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole().name() // Get the string name of the enum
        );

        return ResponseEntity.ok(profileResponse);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            // This is a safety check, though unlikely if security is configured correctly.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        return ResponseEntity.ok("Verified");
    }
}
