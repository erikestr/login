package com.example.login.registration;

import com.example.login.appuser.AppUser;
import com.example.login.appuser.AppUserRole;
import com.example.login.appuser.AppUserService;
import com.example.login.email.EmailResendService;
import com.example.login.email.EmailSender;
import com.example.login.registration.token.ConfirmationToken;
import com.example.login.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private EmailValidator emailValidator;
    private final ConfirmationTokenService confirmationTokenService;
    private EmailResendService emailResendService;

    public String register(RegistrationRequest request) {
        boolean isValidEmail =
                emailValidator.test(request.getEmail());
        if (!isValidEmail){
            throw new IllegalStateException("email not valid");
        }

        String token = appUserService.signUpUser(
                new AppUser(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword(),
                        AppUserRole.USER
                )
        );
        emailResendService.sendEmailService(request.getEmail(), token, request.getFirstName());

        return token;
    }

    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() ->
                        new IllegalStateException("token not found"));                                                  //Token Query to DB and storage to ConfirmationToken instance

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
//            TODO: Resend email confirmation
        }

        confirmationTokenService.setConfirmedAt(token);                                                                 //Update ConfirmedAt by token
        appUserService.enableAppUser(
                confirmationToken.getAppUser().getEmail());                                                             //Update enabled by email
        return "confirmed";
    }
}
