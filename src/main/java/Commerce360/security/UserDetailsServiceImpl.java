package Commerce360.security;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Commerce360.entity.User;
import Commerce360.entity.ApprovalStatus;
import Commerce360.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Check if user is approved
        if (user.getApprovalStatus() != ApprovalStatus.APPROVED) {
            logger.warn("User with email {} attempted to login with status: {}", email, user.getApprovalStatus());
            throw new UsernameNotFoundException(
                    "User account is not approved. Current status: " + user.getApprovalStatus());
        }

        logger.info("Loading user details for email: {}, role: {}", email, user.getRole());

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        logger.info("Created UserDetails with authorities: {}", userDetails.getAuthorities());

        return userDetails;
    }
}
