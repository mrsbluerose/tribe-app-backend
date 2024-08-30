package com.savvato.tribeapp.services;

import com.savvato.tribeapp.controllers.dto.PhraseSequenceDataRequest;
import com.savvato.tribeapp.controllers.dto.PhraseSequenceRequest;
import com.savvato.tribeapp.constants.PhraseTestConstants;
import com.savvato.tribeapp.constants.UserTestConstants;
import com.savvato.tribeapp.dto.AttributeDTO;
import com.savvato.tribeapp.dto.PhraseDTO;
import com.savvato.tribeapp.entities.PhraseSequence;
import com.savvato.tribeapp.repositories.PhraseSequenceRepository;
import com.savvato.tribeapp.repositories.UserPhraseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class AttributesServiceImplTest implements UserTestConstants, PhraseTestConstants {

    @TestConfiguration
    static class AttributesServiceTestContextConfiguration {
        @Bean
        public AttributesService attributesService() {
            return new AttributesServiceImpl();
        }
    }

    @Autowired
    AttributesService attributesService;
    @MockBean
    PhraseService phraseService;
    @MockBean
    UserPhraseRepository userPhraseRepository;
    @MockBean
    PhraseSequenceRepository phraseSequenceRepository;


    @Test
    public void testGetAttributesByUserId_EmptyPhrases() {
        Long userId = 1L;

        // Mock an empty list of phrase sequences
        when(phraseSequenceRepository.findByUserIdOrderByPosition(userId)).thenReturn(Collections.emptyList());

        // Mock an empty Optional from phraseService
        when(phraseService.getPhraseInformationByUserId(userId)).thenReturn(Optional.empty());

        Optional<List<AttributeDTO>> result = attributesService.getAttributesByUserId(userId);

        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }
  
    public void getAttributesByUserIdWhenAttributesExist() {
        Long userId = USER1_ID;
        Map<PhraseDTO, Integer> phraseInformation =
                Map.of(
                        PhraseDTO.builder()
                                .adverb(ADVERB1_WORD)
                                .verb(VERB1_WORD)
                                .noun(NOUN1_WORD)
                                .build(), 1,
                        PhraseDTO.builder()
                                .adverb(ADVERB2_WORD)
                                .verb(VERB2_WORD)
                                .preposition(PREPOSITION2_WORD)
                                .noun(NOUN2_WORD)
                                .build(), 2);
        List<AttributeDTO> attributes = phraseInformation
                .entrySet()
                .stream()
                .map(
                        entry -> AttributeDTO.builder().phrase(entry.getKey()).userCount(entry.getValue()).build()
                )
                .toList();
        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        Optional<List<AttributeDTO>> expected = Optional.of(attributes);
        when(phraseService.getPhraseInformationByUserId(anyLong()))
                .thenReturn(Optional.of(phraseInformation));
        Optional<List<AttributeDTO>> actual = attributesService.getAttributesByUserId(userId);
        verify(phraseService, times(1)).getPhraseInformationByUserId(userIdArgumentCaptor.capture());
        assertEquals(userIdArgumentCaptor.getValue(), userId);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    public void testGetAttributesByUserId_WithPhrases() {
        Long userId = 1L;

        // Create phrase sequences for this user
        List<PhraseSequence> phraseSequences = Arrays.asList(
                new PhraseSequence(userId, 1L, 1),
                new PhraseSequence(userId, 2L, 2)
        );

        // Create a map of PhraseDTO and their counts
        Map<PhraseDTO, Integer> phraseInfo = new HashMap<>();
        PhraseDTO phrase1 = PhraseDTO.builder().id(1L).adverb("enthusiastically").verb("volunteers").preposition("at").noun("UNICEF").build();
        PhraseDTO phrase2 = PhraseDTO.builder().id(2L).adverb("happily").verb("sings").preposition("at").noun("party").build();

        phraseInfo.put(phrase1, 3);
        phraseInfo.put(phrase2, 5);

        // Mock the responses from the repository and the service
        when(phraseSequenceRepository.findByUserIdOrderByPosition(userId)).thenReturn(phraseSequences);
        when(phraseService.getPhraseInformationByUserId(userId)).thenReturn(Optional.of(phraseInfo));

        // Call the method and verify the results
        Optional<List<AttributeDTO>> result = attributesService.getAttributesByUserId(userId);

        assertTrue(result.isPresent());
        List<AttributeDTO> attributes = result.get();

        assertEquals(2, attributes.size());

        // Validate the first attribute
        AttributeDTO attr1 = attributes.get(0);
        assertEquals(1L, attr1.phrase.id);
        assertEquals("enthusiastically", attr1.phrase.adverb);
        assertEquals("volunteers", attr1.phrase.verb);
        assertEquals("at", attr1.phrase.preposition);
        assertEquals("UNICEF", attr1.phrase.noun);
        assertEquals(3, attr1.userCount);
        assertEquals(1, attr1.sequence);

        // Validate the second attribute
        AttributeDTO attr2 = attributes.get(1);
        assertEquals(2L, attr2.phrase.id);
        assertEquals("happily", attr2.phrase.adverb);
        assertEquals("sings", attr2.phrase.verb);
        assertEquals("at", attr2.phrase.preposition);
        assertEquals("party", attr2.phrase.noun);
        assertEquals(5, attr2.userCount);
        assertEquals(2, attr2.sequence);
    }
  
    @Test
    public void testLoadSequence() {
        // Given
        Long userId = 1L;
        PhraseSequenceRequest sequence = new PhraseSequenceRequest(userId, Arrays.asList(
                new PhraseSequenceDataRequest(1L, 1),
                new PhraseSequenceDataRequest(2L, 2)
        ));

        // When
        boolean result = attributesService.loadSequence(sequence);

        // Then
        verify(phraseSequenceRepository, times(1)).storeOrUpdatePhrases(userId, 1L, 1);
        verify(phraseSequenceRepository, times(1)).storeOrUpdatePhrases(userId, 2L, 2);
        assertTrue(result);
    }

    @Test
    public void testUpdatePhraseSequences() {
        // Given
        Long userId = 1L;
        PhraseSequenceDataRequest phrase = new PhraseSequenceDataRequest(1L, 1);

        // When
        attributesService.updatePhraseSequences(userId, phrase);

        // Then
        verify(phraseSequenceRepository, times(1)).storeOrUpdatePhrases(userId, 1L, 1);
    }

    @Test
    public void testLoadSequence_WithEmptyPhrases() {
        // Given
        Long userId = 1L;
        PhraseSequenceRequest sequence = new PhraseSequenceRequest(userId, Collections.emptyList());

        // When
        boolean result = attributesService.loadSequence(sequence);

        // Then
        verify(phraseSequenceRepository, never()).storeOrUpdatePhrases(anyLong(), anyLong(), anyInt());
        assertTrue(result);
    }

    @Test
    public void testLoadSequence_WithNullPhrases() {
        // Given
        Long userId = 1L;
        PhraseSequenceRequest sequence = new PhraseSequenceRequest(userId, null);

        // When and Then
        assertThrows(NullPointerException.class, () -> attributesService.loadSequence(sequence));
    }

}
