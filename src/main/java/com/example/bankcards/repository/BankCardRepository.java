package com.example.bankcards.repository;


import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {

    @Query("SELECT c FROM BankCard c WHERE c.user.id = :userId")
    Page<BankCard> findByUserId(@Param("userId") Long userId, Pageable pageable);

    List<BankCard> findByUserId(Long userId);

    @Query("SELECT c FROM BankCard c WHERE c.user.id = :userId AND c.id = :cardId")
    Optional<BankCard> findByUserIdAndCardId(
            @Param("userId") Long userId,
            @Param("cardId") Long cardId
    );

    @Query("SELECT c FROM BankCard c WHERE c.user = :user AND c.status = 'ACTIVE'")
    List<BankCard> findActiveCardsByUser(User user);
}