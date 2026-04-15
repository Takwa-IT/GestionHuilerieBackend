package Config;

import Models.Utilisateur;
import Repositories.UtilisateurRepository;
import Services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String jwt = extractToken(request);
        log.debug("[JWT] {} {} tokenPresent={}", request.getMethod(), request.getRequestURI(), jwt != null);
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email;
        try {
            email = jwtService.extractEmail(jwt);
            log.debug("[JWT] extracted email={} for {} {}", email, request.getMethod(), request.getRequestURI());
        } catch (Exception ex) {
            log.warn("[JWT] failed to extract email for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (email != null) {
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElse(null);
            log.debug("[JWT] userFound={} for email={} on {} {}", utilisateur != null, email, request.getMethod(), request.getRequestURI());
            if (utilisateur != null && jwtService.isTokenValid(jwt, utilisateur)) {
                log.debug("[JWT] token valid for email={} on {} {}", email, request.getMethod(), request.getRequestURI());
                List<SimpleGrantedAuthority> authorities = utilisateur.getProfil() == null
                        ? List.of()
                        : List.of(new SimpleGrantedAuthority(
                        "ROLE_" + utilisateur.getProfil().getNom().toUpperCase()
                ));
                User principal = new User(
                        utilisateur.getEmail(),
                        utilisateur.getMotDePasse(),
                        authorities
                );
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.warn("[JWT] token invalid or user missing for email={} on {} {}", email, request.getMethod(), request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            String trimmed = authHeader.trim();
            if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return normalizeToken(trimmed.substring(7));
            }
            if (!trimmed.isBlank()) {
                return normalizeToken(trimmed);
            }
        }

        String tokenHeader = request.getHeader("token");
        if (tokenHeader != null && !tokenHeader.isBlank()) {
            return normalizeToken(tokenHeader);
        }

        String accessTokenHeader = request.getHeader("accessToken");
        if (accessTokenHeader != null && !accessTokenHeader.isBlank()) {
            return normalizeToken(accessTokenHeader);
        }

        String xAuthTokenHeader = request.getHeader("X-Auth-Token");
        if (xAuthTokenHeader != null && !xAuthTokenHeader.isBlank()) {
            return normalizeToken(xAuthTokenHeader);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if ("token".equals(name) || "accessToken".equals(name) || "jwt".equals(name)) {
                String value = cookie.getValue();
                if (value != null && !value.isBlank()) {
                    return normalizeToken(value);
                }
            }
        }

        return null;
    }

    private String normalizeToken(String token) {
        String normalized = token == null ? null : token.trim();
        if (normalized == null || normalized.isBlank()) {
            return null;
        }

        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }

        return normalized.isBlank() ? null : normalized;
    }
}


