package com.savvato.tribeapp.services;
import com.savvato.tribeapp.constants.PhraseTestConstants;
import com.savvato.tribeapp.dto.ToBeReviewedDTO;
import com.savvato.tribeapp.entities.*;
import com.savvato.tribeapp.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
public class ReviewSubmittingUserServiceImplTest implements PhraseTestConstants {

    @TestConfiguration
    static class ReviewSubmittingUserServiceTestContextConfiguration {

        @Bean
        public ReviewSubmittingUserService reviewSubmittingUserService() {
            return new ReviewSubmittingUserServiceImpl();
        }
    }

    @Autowired
    ReviewSubmittingUserService reviewSubmittingUserService;

    @MockBean
    ToBeReviewedRepository toBeReviewedRepository;

    @MockBean
    ReviewSubmittingUserRepository reviewSubmittingUserRepository;

    // Test that the method returns the expected DTO versions of the ToBeReviewed phrases associated with a user
    @Test
    public void testGetUserPhrasesToBeReviewedHappyPath() {
        List<Long> toBeReviewedIds = new ArrayList<>(Arrays.asList(1L, 2L));
        Mockito.when(reviewSubmittingUserRepository.findToBeReviewedIdByUserId(anyLong
                ())).thenReturn(toBeReviewedIds);

        //String testWord1 = "test";
        //String testWord2 = "test2";
        ToBeReviewed tbr1 = new ToBeReviewed();

        tbr1.setId(1L);
        tbr1.setHasBeenGroomed(true);
        tbr1.setAdverb(ADVERB1_WORD);
        tbr1.setVerb(VERB1_WORD);
        tbr1.setPreposition(PREPOSITION1_WORD);
        tbr1.setNoun(NOUN1_WORD);

        ToBeReviewed tbr2 = new ToBeReviewed();
        tbr1.setId(2L);
        tbr1.setHasBeenGroomed(true);
        tbr1.setAdverb(ADVERB2_WORD);
        tbr1.setVerb(VERB2_WORD);
        tbr1.setPreposition(PREPOSITION2_WORD);
        tbr1.setNoun(NOUN2_WORD);

        Mockito.when(toBeReviewedRepository.findById(anyLong()))
                .thenReturn(Optional.of(tbr1))
                .thenReturn(Optional.of(tbr2));

        List<ToBeReviewedDTO> expectedDTOs = new ArrayList<>();

        ToBeReviewedDTO tbrDTO1 = ToBeReviewedDTO.builder()
                .id(tbr1.getId())
                .hasBeenGroomed(tbr1.isHasBeenGroomed())
                .adverb(tbr1.getAdverb())
                .verb(tbr1.getVerb())
                .preposition(tbr1.getPreposition())
                .noun(tbr1.getNoun())
                .build();
            expectedDTOs.add(tbrDTO1);

        ToBeReviewedDTO tbrDTO2 = ToBeReviewedDTO.builder()
                .id(tbr2.getId())
                .hasBeenGroomed(tbr2.isHasBeenGroomed())
                .adverb(tbr2.getAdverb())
                .verb(tbr2.getVerb())
                .preposition(tbr2.getPreposition())
                .noun(tbr2.getNoun())
                .build();
        expectedDTOs.add(tbrDTO2);

        List<ToBeReviewedDTO> rtnDTOs = reviewSubmittingUserService.getUserPhrasesToBeReviewed(1L);

        assertEquals(rtnDTOs.get(0).hasBeenGroomed, expectedDTOs.get(0).hasBeenGroomed);
        assertEquals(rtnDTOs.get(0).adverb, expectedDTOs.get(0).adverb);
        assertEquals(rtnDTOs.get(0).verb, expectedDTOs.get(0).verb);
        assertEquals(rtnDTOs.get(0).preposition, expectedDTOs.get(0).preposition);
        assertEquals(rtnDTOs.get(0).noun, expectedDTOs.get(0).noun);

        assertEquals(rtnDTOs.get(1).hasBeenGroomed, expectedDTOs.get(1).hasBeenGroomed);
        assertEquals(rtnDTOs.get(1).adverb, expectedDTOs.get(1).adverb);
        assertEquals(rtnDTOs.get(1).verb, expectedDTOs.get(1).verb);
        assertEquals(rtnDTOs.get(1).preposition, expectedDTOs.get(1).preposition);
        assertEquals(rtnDTOs.get(1).noun, expectedDTOs.get(1).noun);

    }
}