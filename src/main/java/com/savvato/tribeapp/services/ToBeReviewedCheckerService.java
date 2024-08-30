package com.savvato.tribeapp.services;

import com.google.gson.JsonElement;
import com.savvato.tribeapp.entities.ToBeReviewed;

import java.util.Optional;

public interface ToBeReviewedCheckerService {
    void processUngroomedPhrases();
    boolean checkPartOfSpeech(String word, String expectedPartOfSpeech);

    void processUngroomedPhrase(ToBeReviewed tbr);

    Optional<JsonElement> getWordDetails(String word);
}
