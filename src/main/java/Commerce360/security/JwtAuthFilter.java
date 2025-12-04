package Commerce360.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Lazy
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            logger.debug("Auth header: {}", authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.debug("No valid auth header found");
                chain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);

            // Skip token validation for refresh endpoint
            if (request.getRequestURI().equals("/api/auth/refresh")) {
                chain.doFilter(request, response);
                return;
            }

            try {
                String email = jwtUtil.extractUsername(token);
                logger.debug("Extracted email from token: {}", email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    logger.debug("Loaded user details: {}", userDetails);

                    if (jwtUtil.validateToken(token, userDetails)) {
                        logger.debug("Token is valid");
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("Set authentication in security context");
                    } else {
                        logger.debug("Token validation failed");
                    }
                }
            } catch (ExpiredJwtException e) {
                logger.warn("Token expired: {}", e.getMessage());
                // If it's an access token, we can let the client know they need to refresh
                if (!jwtUtil.isRefreshToken(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token expired. Please refresh your token.");
                    return;
                }
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Error processing JWT token", e);
            chain.doFilter(request, response);
        }
    }
}
