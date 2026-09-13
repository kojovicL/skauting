package com.football_club.Auth.controller;

import com.football_club.Auth.dto.JwtAuthRequest;
import com.football_club.Auth.dto.RegisterDTO;
import com.football_club.Auth.dto.UserDTO;
import com.football_club.Auth.dto.UserTokenState;
import com.football_club.Auth.model.User;
import com.football_club.Auth.service.impl.UserService;
import com.football_club.Auth.utils.TokenUtils;
import com.football_club.Scouting.model.enums.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthToken(
            @RequestBody JwtAuthRequest authRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(), authRequest.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            String jwt = tokenUtils.createToken(user.getUsername(), user.getRole(), user.getId());
            
            return ResponseEntity.ok(new UserTokenState(jwt, tokenUtils.getExpiredIn()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or account disabled.");
        }
    }

    @PostMapping(value = "/signup", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> addUser(@RequestBody RegisterDTO request) {
        if (userService.isUserExists(request.getUsername(), request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists.");
        }

        UserDTO user = this.userService.registerUser(request);

        return ResponseEntity.ok("Registration successful.");
    }

    @PatchMapping("/scouts/{id}/region")
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<String> setScoutRegion(@PathVariable Long id, @RequestParam Region region) {
        userService.assignRegionToScout(id, region);
        return ResponseEntity.ok("Region uspešno dodeljen skautu.");
    }
}
