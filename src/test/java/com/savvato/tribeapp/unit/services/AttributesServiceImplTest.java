package com.savvato.tribeapp.unit.services;

import com.savvato.tribeapp.constants.PhraseTestConstants;
import com.savvato.tribeapp.constants.UserTestConstants;
import com.savvato.tribeapp.dto.AttributeDTO;
import com.savvato.tribeapp.dto.PhraseDTO;
import com.savvato.tribeapp.repositories.UserPhraseRepository;
import com.savvato.tribeapp.services.AttributesService;
import com.savvato.tribeapp.services.AttributesServiceImpl;
import com.savvato.tribeapp.services.PhraseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
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
    public void getAttributesByUserIdWhenAttributesDontExist() {
        Long userId = USER1_ID;
        Optional<List<AttributeDTO>> expected = Optional.of(new ArrayList<>());
        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(phraseService.getPhraseInformationByUserId(anyLong()))
                .thenReturn(Optional.empty());

        Optional<List<AttributeDTO>> actual = attributesService.getAttributesByUserId(userId);

        verify(phraseService, times(1)).getPhraseInformationByUserId(userIdArgumentCaptor.capture());
        assertEquals(userIdArgumentCaptor.getValue(), userId);
        assertThat(actual).isEqualTo(expected);
    }
}
