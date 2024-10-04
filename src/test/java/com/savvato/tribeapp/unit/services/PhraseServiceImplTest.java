package com.savvato.tribeapp.unit.services;

import com.savvato.tribeapp.constants.Constants;
import com.savvato.tribeapp.constants.PhraseTestConstants;
import com.savvato.tribeapp.constants.UserTestConstants;
import com.savvato.tribeapp.dto.AttributesApplyPhraseToUserDTO;
import com.savvato.tribeapp.dto.PhraseDTO;
import com.savvato.tribeapp.dto.projections.PhraseWithUserCountDTO;
import com.savvato.tribeapp.entities.*;
import com.savvato.tribeapp.repositories.*;
import com.savvato.tribeapp.services.PhraseService;
import com.savvato.tribeapp.services.PhraseServiceImpl;
import com.savvato.tribeapp.services.UserPhraseService;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith({SpringExtension.class})
public class PhraseServiceImplTest implements UserTestConstants, PhraseTestConstants {

    @TestConfiguration
    static class PhraseServiceTestContextConfiguration {

        @Bean
        public PhraseService phraseService() {
            return new PhraseServiceImpl();
        }

    }

    @Autowired
    PhraseService phraseService;

    @Autowired
    PhraseServiceImpl phraseServiceImpl; // for testing helper method

    @MockBean
    PhraseRepository phraseRepository;

    @MockBean
    AdverbRepository adverbRepository;

    @MockBean
    VerbRepository verbRepository;

    @MockBean
    PrepositionRepository prepositionRepository;

    @MockBean
    NounRepository nounRepository;

    @MockBean
    RejectedPhraseRepository rejectedPhraseRepository;

    @MockBean
    RejectedNonEnglishWordRepository rejectedNonEnglishWordRepository;

    @MockBean
    UserPhraseService userPhraseService;

    @MockBean
    UserPhraseRepository userPhraseRepository;

    @MockBean
    ToBeReviewedRepository toBeReviewedRepository;

    @MockBean
    ReviewSubmittingUserRepository reviewSubmittingUserRepository;


    @Test
    public void isPhraseValidHappyPath() {
        when(rejectedPhraseRepository.findByRejectedPhrase(anyString())).thenReturn(Optional.empty());
        when(rejectedNonEnglishWordRepository.findByWord(anyString())).thenReturn(Optional.empty());
        boolean rtn = phraseService.isPhraseValid(ADVERB1_WORD, VERB1_WORD, PREPOSITION1_WORD, NOUN1_WORD);
        ArgumentCaptor<String> wordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> phraseCaptor = ArgumentCaptor.forClass(String.class);

        verify(rejectedNonEnglishWordRepository, times(4)).findByWord(wordCaptor.capture());
        verify(rejectedPhraseRepository, times(1)).findByRejectedPhrase(phraseCaptor.capture());
        assertThat(wordCaptor.getAllValues()).contains(ADVERB1_WORD.toLowerCase(), VERB1_WORD.toLowerCase(), PREPOSITION1_WORD.toLowerCase(), NOUN1_WORD.toLowerCase()); // all words converted to lower case in isPhraseValid
        assertThat(phraseCaptor.getValue()).contains(ADVERB1_WORD.toLowerCase(), VERB1_WORD.toLowerCase(), PREPOSITION1_WORD.toLowerCase(), NOUN1_WORD.toLowerCase()); // all words converted to lower case in isPhraseValid
        assertTrue(rtn);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"          "})
    public void isPhraseValidWhenAdverbOrPrepositionMissing(String value) {
        String adverb = value;
        String verb = VERB1_WORD;
        String noun = NOUN1_WORD;
        String preposition = value;
        when(rejectedPhraseRepository.findByRejectedPhrase(anyString())).thenReturn(Optional.empty());
        when(rejectedNonEnglishWordRepository.findByWord(anyString())).thenReturn(Optional.empty());
        boolean rtn = phraseService.isPhraseValid(adverb, verb, preposition, noun);
        ArgumentCaptor<String> wordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> phraseCaptor = ArgumentCaptor.forClass(String.class);

        verify(rejectedNonEnglishWordRepository, times(4)).findByWord(wordCaptor.capture());
        verify(rejectedPhraseRepository, times(1)).findByRejectedPhrase(phraseCaptor.capture());
        assertThat(wordCaptor.getAllValues()).contains(adverb, verb.toLowerCase(), noun.toLowerCase(), preposition); // all words converted to lower case in isPhraseValid
        assertThat(phraseCaptor.getValue()).contains(noun.toLowerCase(), verb.toLowerCase()); // all words converted to lower case in isPhraseValid
        assertTrue(rtn);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"          "})
    public void testIsMissingVerbOrNounWhenVerbMissing(String verb) {
        String adverb = ADVERB1_WORD;
        String noun = NOUN1_WORD;
        String preposition = PREPOSITION1_WORD;

        assertThrows(IllegalArgumentException.class, () -> {
            phraseService.isPhraseValid(adverb, verb, preposition, noun);
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"          "})
    public void testIsMissingVerbOrNounWhenNounMissing(String noun) {
        String adverb = "competitively";
        String verb = "plays";
        String preposition = "";

        assertThrows(IllegalArgumentException.class, () -> {
            phraseService.isPhraseValid(adverb, verb, preposition, noun);
        });
    }

    @Test
    public void testIsWordRejectedHappyPath() {

        String rejectedWord = "test";
        RejectedNonEnglishWord rejectedNonEnglishWord = new RejectedNonEnglishWord();
        rejectedNonEnglishWord.setId(1L);
        rejectedNonEnglishWord.setWord(rejectedWord);

        Mockito.when(rejectedNonEnglishWordRepository.findByWord(anyString())).thenReturn(Optional.of(rejectedNonEnglishWord));

        assertFalse(phraseService.isPhraseValid(rejectedWord, rejectedWord, rejectedWord, rejectedWord));
    }
    
    @Test
    public void testIsPhrasePreviouslyRejectedHappyPath() {

        String rejectedString = "test test test test";
        RejectedPhrase rejectedPhrase = new RejectedPhrase();
        rejectedPhrase.setId(1L);
        rejectedPhrase.setRejectedPhrase(rejectedString);

        Mockito.when(rejectedPhraseRepository.findByRejectedPhrase(anyString())).thenReturn(Optional.of(rejectedPhrase));

        assertFalse(phraseService.isPhraseValid("test", "test", "test", "test"));
    }

    // Test that UserPhraseRepository is called once when calling ApplyPhraseToUser and phrase has been approved
    @Test
    public void testApplyPhraseToUserWhenPhraseHasBeenPreviouslyApproved() {

        User user1 = UserTestConstants.getUser1();

        Adverb testAdverb = PhraseTestConstants.getTestAdverb1();
        Verb testVerb = PhraseTestConstants.getTestVerb1();
        Preposition testPreposition = PhraseTestConstants.getTestPreposition1();
        Noun testNoun = PhraseTestConstants.getTestNoun1();

        Phrase testPhrase = PhraseTestConstants.getTestPhrase1();

        UserPhrase userPhrase = new UserPhrase();
        userPhrase.setUserId(user1.getId());
        userPhrase.setPhraseId(testPhrase.getId());

        Mockito.when(adverbRepository.findByWord(anyString())).thenReturn(Optional.of(testAdverb));
        Mockito.when(verbRepository.findByWord(anyString())).thenReturn(Optional.of(testVerb));
        Mockito.when(prepositionRepository.findByWord(anyString())).thenReturn(Optional.of(testPreposition));
        Mockito.when(nounRepository.findByWord(anyString())).thenReturn(Optional.of(testNoun));

        Mockito.when(phraseRepository.findByAdverbIdAndVerbIdAndPrepositionIdAndNounId(any(Long.class), any(Long.class), any(Long.class), any(Long.class))).thenReturn(Optional.of(testPhrase));

        Mockito.when(userPhraseRepository.save(Mockito.any())).thenReturn(userPhrase);

        AttributesApplyPhraseToUserDTO expectedDTO = AttributesApplyPhraseToUserDTO
                .builder()
                .isApproved(true)
                .isRejected(false)
                .isInReview(false)
                .build();

        AttributesApplyPhraseToUserDTO actualDTO = phraseService.applyPhraseToUser(user1.getId(), "testAdverb", "testVerb", "testPreposition", "testNoun");

        verify(userPhraseRepository, times(1)).save(Mockito.any());
        AssertionsForClassTypes.assertThat(actualDTO).usingRecursiveComparison().isEqualTo(expectedDTO);

    }

    // Test that reviewSubmittingUserRepository is called once when calling ApplyPhraseToUser and conditions:
    // phrase has not been approved
    // phrase exists in to_be_reviewed
    @Test
    public void testApplyPhraseToUserWhenPhraseExistsInToBeReviewed() {
        User user1 = UserTestConstants.getUser1();

        String testWord = "test";
        ToBeReviewed toBeReviewed = new ToBeReviewed();
        toBeReviewed.setId(1L);
        toBeReviewed.setHasBeenGroomed(false);
        toBeReviewed.setAdverb(testWord);
        toBeReviewed.setVerb(testWord);
        toBeReviewed.setPreposition(testWord);
        toBeReviewed.setNoun(testWord);

        Mockito.when(adverbRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(verbRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(prepositionRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(nounRepository.findByWord(anyString())).thenReturn(Optional.empty());

        Mockito.when(phraseRepository.findByAdverbIdAndVerbIdAndPrepositionIdAndNounId(any(Long.class), any(Long.class), any(Long.class), any(Long.class))).thenReturn(Optional.empty());

        Mockito.when(toBeReviewedRepository.findByAdverbAndVerbAndNounAndPreposition(anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.of(toBeReviewed));

        AttributesApplyPhraseToUserDTO expectedDTO = AttributesApplyPhraseToUserDTO
                .builder()
                .isApproved(false)
                .isRejected(false)
                .isInReview(true)
                .build();

        AttributesApplyPhraseToUserDTO actualDTO = phraseService.applyPhraseToUser(user1.getId(), testWord, testWord, testWord, testWord);

        verify(reviewSubmittingUserRepository, times(1)).save(Mockito.any());
        AssertionsForClassTypes.assertThat(actualDTO).usingRecursiveComparison().isEqualTo(expectedDTO);
    }

    // Test that reviewSubmittingUserRepository is called once when calling ApplyPhraseToUser and conditions:
    // phrase has not been approved
    // phrase does not exist in to_be_reviewed
    @Test
    public void testApplyPhraseToUserWhenPhraseDoesNotExistInToBeReviewed() {
        User user1 = UserTestConstants.getUser1();

        String testWord = "test";
        ToBeReviewed toBeReviewed = new ToBeReviewed();
        toBeReviewed.setId(1L);
        toBeReviewed.setHasBeenGroomed(false);
        toBeReviewed.setAdverb(testWord);
        toBeReviewed.setVerb(testWord);
        toBeReviewed.setPreposition(testWord);
        toBeReviewed.setNoun(testWord);

        Mockito.when(adverbRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(verbRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(prepositionRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(nounRepository.findByWord(anyString())).thenReturn(Optional.empty());

        Mockito.when(phraseRepository.findByAdverbIdAndVerbIdAndPrepositionIdAndNounId(any(Long.class), any(Long.class), any(Long.class), any(Long.class))).thenReturn(Optional.empty());

        Mockito.when(toBeReviewedRepository.findByAdverbAndVerbAndNounAndPreposition(anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        Mockito.when(toBeReviewedRepository.save(Mockito.any())).thenReturn(toBeReviewed);

        AttributesApplyPhraseToUserDTO expectedDTO = AttributesApplyPhraseToUserDTO
                .builder()
                .isApproved(false)
                .isRejected(false)
                .isInReview(true)
                .build();

        AttributesApplyPhraseToUserDTO actualDTO = phraseService.applyPhraseToUser(user1.getId(), testWord, testWord, testWord, testWord);

        verify(reviewSubmittingUserRepository, times(1)).save(Mockito.any());
        AssertionsForClassTypes.assertThat(actualDTO).usingRecursiveComparison().isEqualTo(expectedDTO);
    }

    @Test
    public void getPhraseInformationByUserId() {
        User user1 = UserTestConstants.getUser1();
        String testWord = "test";
        String testEmptyString = "";
        List<Long> userIds = new ArrayList<>(List.of(1L));
        Integer userCount = 1;
        PhraseWithUserCountDTO phraseWithUserCountDTO = new PhraseWithUserCountDTO(1L, 1L, 1L, 1L, 1L, userCount.longValue());


        Mockito.when(userPhraseService.findPhraseIdsByUserId(anyLong())).thenReturn(Optional.of(userIds));
        Mockito.when(phraseRepository.findPhraseByPhraseId(anyLong())).thenReturn(Optional.of(phraseWithUserCountDTO));
        Mockito.when(adverbRepository.findAdverbById(anyLong())).thenReturn(Optional.of(Constants.NULL_VALUE_WORD));
        Mockito.when(verbRepository.findVerbById(anyLong())).thenReturn(Optional.of(testWord));
        Mockito.when(prepositionRepository.findPrepositionById(anyLong())).thenReturn(Optional.of(Constants.NULL_VALUE_WORD));
        Mockito.when(nounRepository.findNounById(anyLong())).thenReturn(Optional.of(testWord));

        Optional<Map<PhraseDTO, Integer>> optPhraseInformationMap = phraseService.getPhraseInformationByUserId(user1.getId());
        Map<PhraseDTO, Integer> phraseInformationMap = optPhraseInformationMap.get();
        PhraseDTO phrase = phraseInformationMap.keySet().iterator().next(); // get first key
        assertEquals(userCount, phraseInformationMap.get(phrase));
        assertEquals(phrase.adverb, testEmptyString);
        assertEquals(phrase.preposition, testEmptyString);
        assertEquals(phrase.verb, testWord);
        assertEquals(phrase.noun, testWord);
        assertEquals(phraseInformationMap.size(), 1);
    }

    @Test
    public void getPhraseInformationByUserIdWhenNoMatchingPhraseFound() {
        Long userId = USER1_ID;
        Optional<List<Long>> phraseIds = Optional.of(List.of(1L, 2L));
        when(userPhraseService.findPhraseIdsByUserId(userId)).thenReturn(phraseIds);
        when(phraseRepository.findPhraseByPhraseId(anyLong())).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> {
            phraseService.getPhraseInformationByUserId(userId);
        }, "phrase not found");
    }

    @Test
    public void getPhraseInformationByUserIdWhenNoPhraseIdsFound() {
        Long userId = USER1_ID;
        when(userPhraseService.findPhraseIdsByUserId(userId)).thenReturn(Optional.empty());
        Optional<Map<PhraseDTO, Integer>> expected = Optional.empty();

        Optional<Map<PhraseDTO, Integer>> actual = phraseService.getPhraseInformationByUserId(userId);
        verify(phraseRepository, never()).findPhraseByPhraseId(anyLong());
        verify(adverbRepository, never()).findAdverbById(anyLong());
        verify(verbRepository, never()).findVerbById(anyLong());
        verify(nounRepository, never()).findNounById(anyLong());
        verify(prepositionRepository, never()).findPrepositionById(anyLong());
        assertEquals(actual, expected);
    }

    @Test
    public void testApplyPhraseToUserWhenAdverbIsBlank() {
        User user1 = UserTestConstants.getUser1();
        String testAdverbEmpty = "";
        String testAdverbConverted = "nullvalue";
        String testVerb = PhraseTestConstants.getTestVerb1().getWord();
        String testPreposition = PhraseTestConstants.getTestPreposition1().getWord();
        String testNoun = PhraseTestConstants.getTestNoun1().getWord();

        ToBeReviewed tbrSaved = new ToBeReviewed();
        tbrSaved.setId(1L);
        tbrSaved.setHasBeenGroomed(false);
        tbrSaved.setAdverb(testAdverbConverted);
        tbrSaved.setVerb(testVerb);
        tbrSaved.setPreposition(testPreposition);
        tbrSaved.setNoun(testNoun);

        // Should return false if the phrase has not been seen before
        Mockito.when(toBeReviewedRepository.save(any())).thenReturn(tbrSaved);

        AttributesApplyPhraseToUserDTO expectedDTO = AttributesApplyPhraseToUserDTO
                .builder()
                .isApproved(false)
                .isRejected(false)
                .isInReview(true)
                .build();

        AttributesApplyPhraseToUserDTO actualDTO = phraseService.applyPhraseToUser(user1.getId(), testAdverbEmpty, testVerb, testPreposition, testNoun);
        AssertionsForClassTypes.assertThat(actualDTO).usingRecursiveComparison().isEqualTo(expectedDTO);

        // Empty Adverb should be converted to "nullvalue"
        ArgumentCaptor<String> argAdverb = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argVerb = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argPreposition = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argNoun = ArgumentCaptor.forClass(String.class);
        verify(toBeReviewedRepository, times(1)).findByAdverbAndVerbAndNounAndPreposition(argAdverb.capture(), argVerb.capture(), argNoun.capture(), argPreposition.capture());
        assertEquals(argAdverb.getValue(), testAdverbConverted);
    }

    @Test
    public void testApplyPhraseToUserWhenPrepositionIsBlank() {
        User user1 = UserTestConstants.getUser1();
        String testAdverb = PhraseTestConstants.getTestAdverb1().getWord();
        String testVerb = PhraseTestConstants.getTestVerb1().getWord();
        String testPrepositionBlank = "";
        String testPrepositionConverted = "nullvalue";
        String testNoun = PhraseTestConstants.getTestNoun1().getWord();

        ToBeReviewed tbrSaved = new ToBeReviewed();
        tbrSaved.setId(1L);
        tbrSaved.setHasBeenGroomed(false);
        tbrSaved.setAdverb(testAdverb);
        tbrSaved.setVerb(testVerb);
        tbrSaved.setPreposition(testPrepositionConverted);
        tbrSaved.setNoun(testNoun);

        // Should return false if the phrase has not been seen before
        Mockito.when(toBeReviewedRepository.save(any())).thenReturn(tbrSaved);
        when(nounRepository.findByWord(anyString())).thenReturn(Optional.of(new Noun(testNoun)));
        when(verbRepository.findByWord(anyString())).thenReturn(Optional.of(new Verb(testVerb)));

        AttributesApplyPhraseToUserDTO expectedDTO = AttributesApplyPhraseToUserDTO
                .builder()
                .isApproved(false)
                .isRejected(false)
                .isInReview(true)
                .build();

        AttributesApplyPhraseToUserDTO actualDTO = phraseService.applyPhraseToUser(user1.getId(), testAdverb, testVerb, testPrepositionBlank, testNoun);
        AssertionsForClassTypes.assertThat(actualDTO).usingRecursiveComparison().isEqualTo(expectedDTO);

        // Empty Preposition should be converted to "nullvalue"
        ArgumentCaptor<String> argAdverb = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argVerb = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argPreposition = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argNoun = ArgumentCaptor.forClass(String.class);
        verify(toBeReviewedRepository, times(1)).findByAdverbAndVerbAndNounAndPreposition(argAdverb.capture(), argVerb.capture(), argNoun.capture(), argPreposition.capture());
        assertEquals(argPreposition.getValue(), testPrepositionConverted);
    }

    @Test
    public void testConstructPhraseDTOFromPhraseInformationWhenPhraseHasAllFourWords() {
        PhraseDTO expectedDTO = PhraseDTO.builder()
                .id(PhraseTestConstants.PHRASE1_ID)
                .adverb(PhraseTestConstants.ADVERB1_WORD)
                .verb(PhraseTestConstants.VERB1_WORD)
                .preposition(PhraseTestConstants.PREPOSITION1_WORD)
                .noun(PhraseTestConstants.NOUN1_WORD)
                .build();

        when(adverbRepository.findAdverbById(anyLong())).thenReturn(Optional.of(ADVERB1_WORD));
        when(verbRepository.findVerbById(anyLong())).thenReturn(Optional.of(VERB1_WORD));
        when(prepositionRepository.findPrepositionById(anyLong())).thenReturn(Optional.of(PREPOSITION1_WORD));
        when(nounRepository.findNounById(anyLong())).thenReturn(Optional.of(NOUN1_WORD));

        PhraseDTO actualDTO = phraseServiceImpl.constructPhraseDTOFromPhraseInformation(PHRASE1_ID, ADVERB1_ID, VERB1_ID, PREPOSITION1_ID, NOUN1_ID);

        AssertionsForClassTypes.assertThat(actualDTO).usingRecursiveComparison().isEqualTo(expectedDTO);

    }

}
