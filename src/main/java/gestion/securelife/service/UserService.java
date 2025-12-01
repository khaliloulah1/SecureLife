package gestion.securelife.service;

import gestion.securelife.dto.request.LoginRequest;
import gestion.securelife.dto.request.RegisterRequest;
import gestion.securelife.dto.response.AuthResponse;
import gestion.securelife.entity.User;
import gestion.securelife.repository.UserRepository;
import gestion.securelife.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository repo;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder;

    // ============================================================================
    //                                  REGISTER
    // ============================================================================
    public AuthResponse register(RegisterRequest req) {

        log.info("Tentative d’enregistrement pour {}", req.getEmail());

        if (repo.findByEmail(req.getEmail()).isPresent()) {
            log.error("Échec inscription : email {} déjà utilisé", req.getEmail());
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        User u = new User();
        u.setEmail(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setNomComplet(req.getNomComplet());
        u.setRole(req.getRole());

        repo.save(u);

        log.info("Utilisateur {} enregistré avec succès", req.getEmail());

        return new AuthResponse(
                jwt.generateAccessToken(u),
                jwt.generateRefreshToken(u)
        );
    }

    // ============================================================================
    //                                  LOGIN
    // ============================================================================
    public AuthResponse login(LoginRequest req) {

        log.info("Tentative de connexion pour {}", req.getEmail());

        User u = repo.findByEmail(req.getEmail())
                .orElseThrow(() -> {
                    log.error("Login échoué : aucun utilisateur avec l'email {}", req.getEmail());
                    return new RuntimeException("Utilisateur non trouvé");
                });

        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            log.error("Login échoué pour {} : mot de passe incorrect", req.getEmail());
            throw new RuntimeException("Mot de passe incorrect");
        }

        log.info("Utilisateur {} connecté avec succès", req.getEmail());

        return new AuthResponse(
                jwt.generateAccessToken(u),
                jwt.generateRefreshToken(u)
        );
    }
}
