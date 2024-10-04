package com.savvato.tribeapp.repositories;

import com.savvato.tribeapp.entities.PhraseSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PhraseSequenceRepository extends JpaRepository<PhraseSequence, Long> {
    @Query(nativeQuery = true, value = "SELECT * FROM phrase_sequence WHERE user_id = ?1 ORDER BY position")
    List<PhraseSequence> findByUserIdOrderByPosition(Long userId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "INSERT INTO phrase_sequence (user_id, phrase_id, position) VALUES (:userId, :phraseId, :position) " +
            "ON DUPLICATE KEY UPDATE position = VALUES(position)")
    void storeOrUpdatePhrases(@Param("userId") Long userId, @Param("phraseId") Long phraseId, @Param("position") Integer position);

    @Query(nativeQuery = true, value = "SELECT COALESCE(MAX(position), 0) + 1 FROM phrase_sequence WHERE user_id = ?1")
    Integer findNextAvailableSequence(Long userId);

}
