package com.example.login.registration.token;

import com.example.login.appuser.AppUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final AppUserRepository appUserRepository;

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public void saveConfirmationToken(ConfirmationToken token){
        confirmationTokenRepository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public int setConfirmedAt(String token) {
        return confirmationTokenRepository.updateConfirmedAt(
                token, LocalDateTime.now());
    }

    public ConfirmationToken getConfirmedAt(Long id){
        return confirmationTokenRepository.findAllByAppUser_Id(id);
    }

    public void updateToken(Long id, String token){
        confirmationTokenRepository.updateTokenByAppUser_Id(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                id
        );
    }
}
