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

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
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
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authHeader.substring(7).trim();
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
                    return value.trim();
                }
            }
        }

        return null;
    }
}
