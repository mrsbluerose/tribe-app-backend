package com.savvato.tribeapp.services;

import com.savvato.tribeapp.dto.PhraseDTO;
import com.savvato.tribeapp.dto.AttributesApplyPhraseToUserDTO;

import java.util.Map;
import java.util.Optional;

public interface PhraseService {

    boolean isPhraseValid(String adverb, String verb, String preposition, String noun);

    AttributesApplyPhraseToUserDTO applyPhraseToUser(Long userId, String adverb, String verb, String preposition, String noun);

    Optional<Long> findPreviouslyApprovedPhraseId(String adverb, String verb, String preposition, String noun);

    Optional<Map<PhraseDTO, Integer>> getPhraseInformationByUserId(Long userId);
}
