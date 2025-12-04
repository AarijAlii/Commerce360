package Commerce360.aspect;

import Commerce360.service.RateLimiterService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RateLimitAspect {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);

    @Autowired
    private RateLimiterService rateLimiterService;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String clientId = request.getRemoteAddr();
        String endpoint = request.getRequestURI();

        logger.debug("Rate limit check for client {} on endpoint {}", clientId, endpoint);

        if (rateLimiterService.tryAcquire(clientId)) {
            logger.debug("Request allowed for client {} on endpoint {}", clientId, endpoint);
            return joinPoint.proceed();
        } else {
            logger.warn("Rate limit exceeded for client {} on endpoint {}", clientId, endpoint);
            return new ResponseEntity<>("Rate limit exceeded. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}