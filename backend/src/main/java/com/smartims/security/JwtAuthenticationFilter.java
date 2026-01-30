package com.smartims.security;

import com.smartims.entity.User;
import com.smartims.repository.UserRepository;
import com.smartims.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.io.IOException;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserRepository userRepository
    ) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException, java.io.IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired token"
            );
            return;
        }

        Claims claims = jwtUtil.extractAllClaims(token);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        Integer tokenVersionFromJwt =
                claims.get("tokenVersion", Integer.class);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null
                || Boolean.TRUE.equals(user.getLocked())
                || Boolean.FALSE.equals(user.getEnabled())) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "User account is locked or disabled"
            );
            return;
        }

        if (!user.getTokenVersion().equals(tokenVersionFromJwt)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token has been invalidated. Please login again."
            );
            return;
        }

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        authorities
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

}

