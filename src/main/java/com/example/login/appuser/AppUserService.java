package com.example.login.appuser;

import com.example.login.email.EmailResendService;
import com.example.login.registration.RegistrationService;
import com.example.login.registration.token.ConfirmationToken;
import com.example.login.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final static String USER_NOT_FOUND =
            "user with email %s not found";
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private EmailResendService emailResendService;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format(USER_NOT_FOUND, email)));
    }

    public String signUpUser(AppUser appUser){
        boolean userExists = appUserRepository.findByEmail(appUser.getEmail())
                .isPresent();
        if (userExists){
            AppUser userAlreadyExists = appUserRepository.findAllByEmail(appUser.getEmail());

            ConfirmationToken confirmedToken = confirmationTokenService
                    .getConfirmedAt(userAlreadyExists.getId());
            if (confirmedToken.getConfirmedAt() == null){
//              TODO if expiresAt its done, make an update to Token --> DONE
//              TODO resend --> DONE
                if (confirmedToken.getExpiresAt().isBefore(LocalDateTime.now())){

                    String newToken = UUID.randomUUID().toString();

                    confirmationTokenService.updateToken(
                            userAlreadyExists.getId(),
                                newToken
                    );

                    emailResendService.sendEmailService(
                            userAlreadyExists.getEmail(),
                            newToken,
                            userAlreadyExists.getFirstName()
                    );

                    throw new IllegalStateException("email already taken & " +
                            "confirmation link expired, " +
                            "resending confirmation email");
                }
                emailResendService.sendEmailService(
                        userAlreadyExists.getEmail(),
                        confirmedToken.getToken(),
                        userAlreadyExists.getFirstName()
                );
                throw new IllegalStateException("email already taken & " +
                        "not confirmed, " +
                        "resending confirmation email");
            }
            throw new IllegalStateException("email already taken");
        }

        String encodedPassword = bCryptPasswordEncoder
                .encode(appUser.getPassword());

        appUser.setPassword(encodedPassword);

        saveNewRegister(appUser);                                                                                //SAVE NEW REGISTER

//      TODO: Send confirmation Token --> DONE

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        return token;
    }

    public void saveNewRegister(AppUser appUser){
        appUserRepository.save(appUser);
    }

    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }
}
