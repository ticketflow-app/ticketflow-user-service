package com.ticketflow.user.user.infrastructure.security;

import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.infrastructure.adapter.out.security.AuthenticatedUser;
import com.ticketflow.user.user.infrastructure.adapter.out.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String REQUEST_ATTRIBUTE_USER_ID = "USER_ID";
    public static final String REQUEST_ATTRIBUTE_USER_ROLE = "USER_ROLE";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtTokenProvider.parse(token);
                UUID userId = UUID.fromString(claims.getSubject());
                UserRole role = UserRole.from(claims.get("role", String.class));
                String email = claims.get("email", String.class);

                AuthenticatedUser principal = new AuthenticatedUser(userId, email, role);
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                request.setAttribute(REQUEST_ATTRIBUTE_USER_ID, userId);
                request.setAttribute(REQUEST_ATTRIBUTE_USER_ROLE, role);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}