package com.savvato.tribeapp.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.savvato.tribeapp.constants.Constants;
import com.savvato.tribeapp.entities.RejectedPhrase;
import com.savvato.tribeapp.entities.ReviewSubmittingUser;
import com.savvato.tribeapp.entities.ToBeReviewed;
import com.savvato.tribeapp.repositories.RejectedPhraseRepository;
import com.savvato.tribeapp.repositories.ReviewSubmittingUserRepository;
import com.savvato.tribeapp.repositories.ToBeReviewedRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class ToBeReviewedCheckerServiceImpl implements ToBeReviewedCheckerService {

    @Autowired
    ToBeReviewedRepository toBeReviewedRepository;
    @Autowired
    RejectedPhraseRepository rejectedPhraseRepository;
    @Autowired
    ReviewSubmittingUserRepository reviewSubmittingUserRepository;

    @Autowired
    RestTemplate restTemplate;

    @Value("${MERRIAM_WEBSTER_DICTIONARY_API_KEY}")
    private String apiKey;

    @Scheduled(fixedDelayString = "PT10M")
    @Override
    public void processUngroomedPhrases() {
        List<ToBeReviewed> ungroomedPhrases = toBeReviewedRepository.getAllUngroomed();

        if (!ungroomedPhrases.isEmpty()) {
            log.info("Beginning to process ungroomed phrases.");

            for (ToBeReviewed tbr : ungroomedPhrases) {
                processUngroomedPhrase(tbr);
            }
        }
    }

    @Override
    public void processUngroomedPhrase(ToBeReviewed tbr) {

        boolean validPhrase = checkPartOfSpeech(tbr.getNoun(), "noun")
                && checkPartOfSpeech(tbr.getVerb(), "verb")
                && (tbr.getAdverb().equals(Constants.NULL_VALUE_WORD) || checkPartOfSpeech(tbr.getAdverb(), "adverb"))
                && (tbr.getPreposition().equals(Constants.NULL_VALUE_WORD) || checkPartOfSpeech(tbr.getPreposition(), "preposition"));

        if (validPhrase) {
            tbr.setHasBeenGroomed(true);
            toBeReviewedRepository.save(tbr);
        } else {
            log.warn("Phrase is invalid.");
            rejectedPhraseRepository.save(new RejectedPhrase(tbr.toString()));
            // TODO: Create notification for users when their submitted phrase has been rejected after review. Jira TRIB-153
            ReviewSubmittingUser rsu = new ReviewSubmittingUser(reviewSubmittingUserRepository.findUserIdByToBeReviewedId(tbr.getId()), tbr.getId());
            reviewSubmittingUserRepository.delete(rsu);
            toBeReviewedRepository.deleteById(tbr.getId());
        }
    }

    @Override
    public boolean checkPartOfSpeech(String word, String expectedPartOfSpeech) {
        Optional<JsonElement> wordDetails = getWordDetails(word);
        if (wordDetails.isEmpty()) {
            return false;
        } else {
            JsonArray definitions;
            Set<String> partsOfSpeech = new HashSet<>();

            definitions = wordDetails.get().getAsJsonArray();

            for (int i = 0; i < definitions.size(); i++) {
                JsonObject definition = definitions.get(i).getAsJsonObject();
                partsOfSpeech.add(definition.get("fl").getAsString());
            }

            if (partsOfSpeech.contains(expectedPartOfSpeech)) {
                return true;
            } else {
                log.warn(word + " isn't a(n) " + expectedPartOfSpeech + "!");
                return false;
            }
        }
    }

    @Override
    public Optional<JsonElement> getWordDetails(String word) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String url = "https://www.dictionaryapi.com/api/v3/references/collegiate/json/" + word + "?key=" + apiKey;
        HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = null;
        Optional responseJson = Optional.empty();
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (RestClientException e) {
            log.warn(word + " isn't an English word!");
            return responseJson;
        }

        responseJson = Optional.of(new JsonParser().parseString(response.getBody()));
        return responseJson;
    }

}
