package com.savvato.tribeapp.unit.services;

import com.google.gson.JsonParser;
import com.savvato.tribeapp.repositories.RejectedPhraseRepository;
import com.savvato.tribeapp.repositories.ReviewSubmittingUserRepository;
import com.savvato.tribeapp.repositories.ToBeReviewedRepository;
import com.savvato.tribeapp.services.ToBeReviewedCheckerService;
import com.savvato.tribeapp.services.ToBeReviewedCheckerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class ToBeReviewedCheckerServiceImpl_checkPartOfSpeechTest {
    @TestConfiguration
    static class ToBeReviewedCheckerServiceImplTestContextConfiguration {
        @Bean
        public ToBeReviewedCheckerService toBeReviewedCheckerService() {
            return new ToBeReviewedCheckerServiceImpl();
        }
    }

    @MockBean
    private ToBeReviewedRepository toBeReviewedRepository;
    @MockBean
    private RejectedPhraseRepository rejectedPhraseRepository;
    @MockBean
    private ReviewSubmittingUserRepository reviewSubmittingUserRepository;
    @MockBean
    private RestTemplate restTemplate;


    @Autowired
    private ToBeReviewedCheckerService toBeReviewedCheckerService;

    @Test
    public void checkPartOfSpeech() {
        assertTrue(_testCheckPartOfSpeech("walk", "verb", "verb"));
    }

    @Test
    public void checkPartOfSpeechWhenNoDefinitionFound() {
        assertFalse(_testCheckPartOfSpeech("walk", "verb", "", true));
    }

    @Test
    public void checkPartOfSpeechWhenExpectedPartOfSpeechDoesntMatchWord() {
        assertFalse(_testCheckPartOfSpeech("walk", "noise", "noun"));
    }

    @Test
    public void processUngroomedPhraseComponentWhenComponentIsIncorrectPartOfSpeech() {
        assertFalse(_testCheckPartOfSpeech("is", "noun", "adjective"));
    }

    private boolean _testCheckPartOfSpeech(String word, String expectedPartOfSpeech, String apiResponsesPartOfSpeech) {
        return _testCheckPartOfSpeech(word, expectedPartOfSpeech, apiResponsesPartOfSpeech, false);
    }

    private boolean _testCheckPartOfSpeech(String word, String expectedPartOfSpeech, String apiResponsesPartOfSpeech, boolean emptyWordDetails) {
        ToBeReviewedCheckerService toBeReviewedCheckerServiceSpy = spy(toBeReviewedCheckerService);

        if (emptyWordDetails) {
            doReturn(
                    Optional.empty())
                    .when(toBeReviewedCheckerServiceSpy).getWordDetails(word);
        } else {
            doReturn(Optional.of(
                    new JsonParser()
                            .parseString("[{\"fl\":\"" + apiResponsesPartOfSpeech + "\", \"other-definition-fields\":\"boo bah boo\"}]")))
                    .when(toBeReviewedCheckerServiceSpy).getWordDetails(word);
        }

        return toBeReviewedCheckerServiceSpy.checkPartOfSpeech(word, expectedPartOfSpeech);
    }
}