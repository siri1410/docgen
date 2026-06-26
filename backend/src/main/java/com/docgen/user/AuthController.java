package com.docgen.user;

import com.docgen.common.Exceptions.BadRequestException;
import com.docgen.security.JwtService;
import com.docgen.user.Repositories.OrganizationRepository;
import com.docgen.user.Repositories.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal auth surface: register + login returning a JWT. This is intentionally a stub —
 * enough to demonstrate the full JWT/role wiring without blocking the demo (dev profile is open).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final OrganizationRepository organizations;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository users, OrganizationRepository organizations,
                         PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.organizations = organizations;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank String password,
            String displayName) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record AuthResponse(String token, String userId, String email, List<String> roles) {}

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        if (users.existsByEmail(req.email())) {
            throw new BadRequestException("Email already registered");
        }
        Organization org = organizations.findBySlug("default")
                .orElseGet(() -> organizations.save(new Organization("Default Org", "default")));
        User user = new User(
                req.email(),
                passwordEncoder.encode(req.password()),
                req.displayName(),
                org.getId(),
                Set.of("EDITOR"));
        users.save(user);
        return token(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        User user = users.findByEmail(req.email())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        return token(user);
    }

    @GetMapping("/config")
    public Object config() {
        return jwtService.describe();
    }

    private AuthResponse token(User user) {
        List<String> roles = List.copyOf(user.getRoles());
        String jwt = jwtService.issue(user.getId().toString(), user.getEmail(), roles);
        return new AuthResponse(jwt, user.getId().toString(), user.getEmail(), roles);
    }
}
