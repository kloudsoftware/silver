package software.kloud.silver.config.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationTokenFilter extends OncePerRequestFilter {
    private final AuthenticationProvider provider;

    @Value("${silver.key}")
    private String key;

    @Autowired
    public AuthenticationTokenFilter(AuthenticationProvider provider) {
        this.provider = provider;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.printf("Silver key: %s", key);
        final String header = request.getHeader("Authorization");
        if (null == header) {
            filterChain.doFilter(request, response);
            return;
        }

        final String tokenCode = header.replace("Bearer ", "");

        if(key.equals(tokenCode)) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("username", "password");

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(provider.authenticate(authentication));
        }



        // continue through the filter chain
        filterChain.doFilter(request, response);
    }
}

