package com.savvato.tribeapp.unit.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.savvato.tribeapp.entities.ToBeReviewed;
import com.savvato.tribeapp.repositories.RejectedPhraseRepository;
import com.savvato.tribeapp.repositories.ReviewSubmittingUserRepository;
import com.savvato.tribeapp.repositories.ToBeReviewedRepository;
import com.savvato.tribeapp.services.PhraseServiceImpl;
import com.savvato.tribeapp.services.ToBeReviewedCheckerService;
import com.savvato.tribeapp.services.ToBeReviewedCheckerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class ToBeReviewedCheckerServiceImplTest {
    @TestConfiguration
    static class ToBeReviewedCheckerServiceImplTestContextConfiguration {
        @Bean
        public ToBeReviewedCheckerService toBeReviewedCheckerService() {
            return new ToBeReviewedCheckerServiceImpl();
        }
    }

    @Autowired
    private ToBeReviewedCheckerService toBeReviewedCheckerService;

    @MockBean
    private PhraseServiceImpl phraseService;
    @MockBean
    private ToBeReviewedRepository toBeReviewedRepository;
    @MockBean
    private RejectedPhraseRepository rejectedPhraseRepository;

    @MockBean
    private ReviewSubmittingUserRepository reviewSubmittingUserRepository;

    @MockBean
    private RestTemplate restTemplate;


    @Test
    public void processUngroomedPhrases() {
        ToBeReviewed tbr1 = new ToBeReviewed(1L, false, "competitively", "plays", "", "chess");
        ToBeReviewed tbr2 = new ToBeReviewed(2L, false, "enthusiastically", "volunteers", "for", "UNICEF");
        List<ToBeReviewed> ungroomedPhrases = List.of(tbr1, tbr2);
        ArgumentCaptor<ToBeReviewed> tbrArgumentCaptor = ArgumentCaptor.forClass(ToBeReviewed.class);
        ToBeReviewedCheckerService toBeReviewedCheckerServiceSpy = spy(toBeReviewedCheckerService);
        when(toBeReviewedRepository.getAllUngroomed()).thenReturn(ungroomedPhrases);
        doNothing().when(toBeReviewedCheckerServiceSpy).processUngroomedPhrase(any(ToBeReviewed.class));
        toBeReviewedCheckerServiceSpy.processUngroomedPhrases();
        verify(toBeReviewedCheckerServiceSpy, times(ungroomedPhrases.size())).processUngroomedPhrase(tbrArgumentCaptor.capture());
        assertThat(tbrArgumentCaptor.getAllValues()).usingRecursiveComparison().isEqualTo(ungroomedPhrases);
    }

    @Test
    public void getWordDetails() {
        String word = "competitively";
        String responseBody = "[{\"fl\":\"noun\", \"other-definition-fields\":\"boo bah boo\"}]";

        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        JsonElement wordDetails =
                new JsonParser()
                        .parseString(responseBody);

        Optional<JsonElement> expected = Optional.of(wordDetails);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        Optional<JsonElement> actual = toBeReviewedCheckerService.getWordDetails(word);

        assertThat(expected).usingRecursiveComparison().isEqualTo(actual);
    }

    @Test
    public void getWordDetailsWhenErrorIsThrown() {
        String word = "competitively";
        String errorMessage = "Something went wrong.";
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenThrow(new RestClientException(errorMessage));

        assertEquals(toBeReviewedCheckerService.getWordDetails(word), Optional.empty());
    }

    @Test
    public void processUngroomedPhraseWhenPhraseValid() {
        ToBeReviewed tbr = new ToBeReviewed(1L, false, "competitively", "plays", "", "chess");
        ToBeReviewedCheckerService toBeReviewedCheckerServiceSpy = spy(toBeReviewedCheckerService);
        doReturn(true)
                .when(toBeReviewedCheckerServiceSpy)
                .checkPartOfSpeech(Mockito.any(), Mockito.any());
        toBeReviewedCheckerServiceSpy.processUngroomedPhrase(tbr);
        ArgumentCaptor<ToBeReviewed> tbrArgumentCaptor = ArgumentCaptor.forClass(ToBeReviewed.class);
        // verify tbr was groomed
        assertEquals(true, tbr.isHasBeenGroomed());

        verify(rejectedPhraseRepository, times(0)).save(Mockito.any());
        verify(reviewSubmittingUserRepository, times(0)).delete(Mockito.any());
        verify(toBeReviewedRepository, times(0)).deleteById(Mockito.any());
        verify(toBeReviewedRepository, times(1)).save(tbrArgumentCaptor.capture());
        assertThat(tbrArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(tbr);
    }

    @Test
    public void processUngroomedPhraseWhenPhraseInvalid() {
        ToBeReviewed tbr = new ToBeReviewed(1L, false, "nonsense", "nonsense", "nonsense", "nonsense");
        ToBeReviewedCheckerService toBeReviewedCheckerServiceSpy = spy(toBeReviewedCheckerService);
        doReturn(false)
                .when(toBeReviewedCheckerServiceSpy)
                .checkPartOfSpeech(Mockito.any(), Mockito.any());
        toBeReviewedCheckerServiceSpy.processUngroomedPhrase(tbr);
        ArgumentCaptor<ToBeReviewed> tbrArgumentCaptor = ArgumentCaptor.forClass(ToBeReviewed.class);
        // verify tbr was groomed
        assertEquals(false, tbr.isHasBeenGroomed());

        verify(rejectedPhraseRepository, times(1)).save(Mockito.any());
        verify(reviewSubmittingUserRepository, times(1)).delete(Mockito.any());
        verify(toBeReviewedRepository, times(1)).deleteById(Mockito.any());
        verify(toBeReviewedRepository, times(0)).save(tbrArgumentCaptor.capture());
    }

}
