package software.kloud.silver.config.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class SecurityControllerAdvice {
    private static final Logger log = LoggerFactory.getLogger(SecurityControllerAdvice.class);

    @ExceptionHandler({SecurityException.class})
    public String handleSecurityException(SecurityException e) {
        log.error("Security exception" ,e);
        return "Incorrect auth header present";
    }
}
