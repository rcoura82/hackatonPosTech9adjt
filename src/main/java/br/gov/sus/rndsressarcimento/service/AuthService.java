package br.gov.sus.rndsressarcimento.service;

import br.gov.sus.rndsressarcimento.dto.AuthRequest;
import br.gov.sus.rndsressarcimento.dto.AuthResponse;
import br.gov.sus.rndsressarcimento.exception.BusinessException;
import br.gov.sus.rndsressarcimento.persistence.entity.AppUserEntity;
import br.gov.sus.rndsressarcimento.persistence.entity.UserRole;
import br.gov.sus.rndsressarcimento.persistence.repository.AppUserRepository;
import br.gov.sus.rndsressarcimento.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(AuthRequest request) {
        AppUserEntity user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("Credenciais invalidas"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Credenciais invalidas");
        }

        String token = jwtService.generateToken(user.getUsername(), Map.of(
                "role", user.getRole().name(),
                "organizationId", user.getOrganizationId()
        ));
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(), user.getRole().name());
    }

    public void seedDefaultUsers() {
        createIfMissing("sus.admin", "sus123", UserRole.SUS, "SUS-NACIONAL");
        createIfMissing("operadora.admin", "op123", UserRole.OPERADORA, "OPERADORA-DEMO");
    }

    private void createIfMissing(String username, String password, UserRole role, String orgId) {
        if (appUserRepository.findByUsername(username).isPresent()) {
            return;
        }
        AppUserEntity entity = new AppUserEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode(password));
        entity.setRole(role);
        entity.setOrganizationId(orgId);
        entity.setEnabled(true);
        entity.setCreatedAt(Instant.now());
        appUserRepository.save(entity);
    }
}
