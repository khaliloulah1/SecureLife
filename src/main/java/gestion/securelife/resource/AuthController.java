package gestion.securelife.resource;

import gestion.securelife.dto.request.*;
import gestion.securelife.dto.response.*;
import gestion.securelife.entity.User;
import gestion.securelife.repository.UserRepository;
import gestion.securelife.security.JwtService;
import gestion.securelife.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "Gestion de l'inscription, connexion et tokens JWT")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository repo;

    // =======================================================================================
    //                                        REGISTER
    // =======================================================================================
    @Operation(summary = "Créer un utilisateur")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        log.info("Demande d'inscription reçue pour email {}", req.getEmail());
        AuthResponse res = userService.register(req);
        log.info("Utilisateur créé avec succès : {}", req.getEmail());
        return ResponseEntity.status(201).body(res);
    }

    // =======================================================================================
    //                                            LOGIN
    // =======================================================================================
    @Operation(summary = "Connexion utilisateur")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        log.info("Tentative de connexion pour {}", req.getEmail());
        AuthResponse res = userService.login(req);
        log.info("Connexion réussie pour {}", req.getEmail());
        return ResponseEntity.ok(res);
    }

    // =======================================================================================
    //                                          REFRESH TOKEN
    // =======================================================================================
    @Operation(summary = "Rafraîchir le token JWT")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String token) {
        log.info("Demande de refresh token reçue");

        String email = jwtService.extractUsername(token);
        User user = repo.findByEmail(email).orElseThrow();

        log.info("Token rafraîchi pour {}", email);

        return ResponseEntity.ok(
                new AuthResponse(
                        jwtService.generateAccessToken(user),
                        jwtService.generateRefreshToken(user)
                )
        );
    }

    // =======================================================================================
    //                                             PROFILE
    // =======================================================================================
    @Operation(summary = "Obtenir les informations de l'utilisateur connecté")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication auth) {
        User user = (User) auth.getPrincipal();

        log.info("Récupération du profil pour {}", user.getEmail());

        return ResponseEntity.ok(
                new UserProfileResponse(
                        user.getEmail(),
                        user.getNomComplet(),
                        user.getRole()
                )
        );
    }
}
