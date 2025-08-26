package com.ecom.config;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstraint;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserService userService;

    private final UserRepository userRepository;

    public AuthFailureHandlerImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("username");
        UserDtls userDtls = userRepository.findByEmail(email);

        if (userDtls != null && userDtls.getIsEnable()) {

            if (userDtls.getAccountNonLocked()) {

                if (userDtls.getFailedAttempts() < AppConstraint.ATTEMPT_TIME) {
                    userService.increaseFailedAttempt(userDtls);
                } else {
                    userService.userAccountLock(userDtls);
                    exception = new LockedException("Your account has been locked due to too many failed attempts.");
                }

            } else { // ya está bloqueada

                if (userService.unlockAccountTimeExpired(userDtls)) {
                    exception = new LockedException("Your account was locked but lock duration expired. Please try to login again.");
                } else {
                    exception = new LockedException("Your account is currently locked. Please wait before trying again.");
                }
            }
        }

        super.onAuthenticationFailure(request, response, exception);
    }

}
