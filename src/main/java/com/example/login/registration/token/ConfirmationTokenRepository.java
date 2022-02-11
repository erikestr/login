package com.example.login.registration.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    Optional<ConfirmationToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("UPDATE ConfirmationToken c " +
            "SET c.confirmedAt = ?2 " +
            "WHERE c.token = ?1")
    int updateConfirmedAt(String token,
                          LocalDateTime confirmedAt);

    ConfirmationToken findAllByAppUser_Id(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE ConfirmationToken c SET " +
            "c.token = ?1," +
            "c.createdAt = ?2," +
            "c.expiresAt = ?3 " +
            "WHERE c.appUser.id = ?4")
    void updateTokenByAppUser_Id(String token, LocalDateTime now, LocalDateTime plusMinutes, Long id);
}
