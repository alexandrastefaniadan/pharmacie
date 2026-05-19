package ma.pharmacie.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.pharmacie.auth.dto.ChangePasswordRequest;
import ma.pharmacie.auth.dto.LoginRequest;
import ma.pharmacie.auth.dto.UserInfo;
import ma.pharmacie.auth.entity.AppUser;
import ma.pharmacie.auth.repo.AppUserRepository;
import ma.pharmacie.common.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Authentication endpoints used by the SPA.
 *
 * <pre>
 *   POST /api/v1/auth/login   -> 200 + {username, roles}     | 401
 *   POST /api/v1/auth/logout  -> handled by Spring Security  -> 204
 *   GET  /api/v1/auth/me      -> 200 + {username, roles}     | 401
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @Operation(summary = "Log in. Creates an HTTP session on success.")
    @PostMapping("/login")
    public UserInfo login(@Valid @RequestBody LoginRequest req,
                          HttpServletRequest httpReq,
                          HttpServletResponse httpRes) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));

            // Persist the authentication into a fresh HTTP session.
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);
            securityContextRepository.saveContext(ctx, httpReq, httpRes);

            return toUserInfo(auth);
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
    }

    @Operation(summary = "Return the currently authenticated user.")
    @GetMapping("/me")
    public ResponseEntity<UserInfo> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(toUserInfo(auth));
    }

    @Operation(summary = "Change the current user's password.",
            description = "Requires the current password. The new password must be at least 10 characters. "
                    + "Returns 401 if the current password is wrong.")
    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void changePassword(@Valid @RequestBody ChangePasswordRequest req, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        AppUser user = users.findByUsernameIgnoreCase(auth.getName())
                .orElseThrow(() -> NotFoundException.of("user", auth.getName()));

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }
        if (passwordEncoder.matches(req.newPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le nouveau mot de passe doit être différent de l'actuel.");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        // Managed entity: persisted on transaction commit.
    }

    private static UserInfo toUserInfo(Authentication auth) {
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .toList();
        return new UserInfo(auth.getName(), roles);
    }
}

