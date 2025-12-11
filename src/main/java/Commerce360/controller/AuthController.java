package Commerce360.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import Commerce360.security.JwtUtil;
import Commerce360.security.LoginRequest;
import Commerce360.security.RefreshRequest;
import Commerce360.security.AuthResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and token management endpoints")
public class AuthController {

    // AUTHENTICATION MANAGER
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user with email and password. Returns JWT access token and refresh token.", tags = {
            "Authentication" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @SecurityRequirement(name = "")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            final String accessToken = jwtUtil.generateAccessToken(userDetails);
            final String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Access Token", description = "Get new access and refresh tokens using a valid refresh token.", tags = {
            "Authentication" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "401", description = "Refresh token expired")
    })
    @SecurityRequirement(name = "")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            if (!jwtUtil.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid refresh token");
            }

            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(refreshToken, userDetails)) {
                String newAccessToken = jwtUtil.generateAccessToken(userDetails);
                String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
                return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }
}
