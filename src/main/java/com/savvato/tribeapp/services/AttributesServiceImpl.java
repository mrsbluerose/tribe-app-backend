package com.savvato.tribeapp.services;

import com.savvato.tribeapp.controllers.dto.PhraseSequenceDataRequest;
import com.savvato.tribeapp.controllers.dto.PhraseSequenceRequest;
import com.savvato.tribeapp.dto.AttributeDTO;
import com.savvato.tribeapp.dto.PhraseDTO;
import com.savvato.tribeapp.entities.PhraseSequence;
import com.savvato.tribeapp.repositories.PhraseSequenceRepository;
import com.savvato.tribeapp.repositories.UserPhraseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttributesServiceImpl implements AttributesService {

    @Autowired
    PhraseService phraseService;

    @Autowired
    UserPhraseRepository userPhraseRepository;

    @Autowired
    private PhraseSequenceRepository phraseSequenceRepository;

    @Override
    public Optional<List<AttributeDTO>> getAttributesByUserId(Long userId) {

        List<PhraseSequence> phraseSequences = phraseSequenceRepository.findByUserIdOrderByPosition(userId);

        Map<Long, Integer> phraseIdToSequenceMap = phraseSequences.stream()
                .collect(Collectors.toMap(
                        PhraseSequence::getPhraseId,
                        PhraseSequence::getPosition
                ));

        // Get all user phrases as phraseDTOs
        Optional<Map<PhraseDTO, Integer>> optUserPhraseDTOs = phraseService.getPhraseInformationByUserId(userId);

        // If there are phrases, build DTO and add to attributes list
        if (optUserPhraseDTOs.isPresent()) {
            Map<PhraseDTO, Integer> phraseDTOUserCountMap = optUserPhraseDTOs.get();

            List<AttributeDTO> attributes = phraseDTOUserCountMap
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        PhraseDTO phraseDTO = entry.getKey();
                        Integer userCount = entry.getValue();

                        // Populate the sequence in PhraseDTO
                        int sequence= phraseIdToSequenceMap.get(phraseDTO.id); // Find sequence
                        List<PhraseSequence> phrases = phraseSequenceRepository.findByUserIdOrderByPosition(userId);
                        return AttributeDTO.builder()
                                .phrase(phraseDTO)
                                .userCount(userCount)
                                .sequence(sequence) // No change to user count
                                .build();
                    })
                    .collect(Collectors.toList());
            attributes.sort(Comparator.comparingLong(a -> (a.phrase.id)));

            return Optional.of(attributes);
        }

        // If no phrases found, return an empty list
        return Optional.of(Collections.emptyList());
    }

    //create a senario where false is returned
    @Transactional
    public boolean loadSequence(PhraseSequenceRequest sequence){
        for (PhraseSequenceDataRequest phrase : sequence.getPhrases()) {
            updatePhraseSequences(sequence.getUserId(), phrase);
        }

        return true;
    }
    @Transactional
    public void updatePhraseSequences(long userId, PhraseSequenceDataRequest phrase) {

        // Iterate over the list of phrases to update their positio
            phraseSequenceRepository.storeOrUpdatePhrases(
                    userId,
                    phrase.phraseId,
                    phrase.sequence
            );

    }
}
