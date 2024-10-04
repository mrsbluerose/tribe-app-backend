package com.savvato.tribeapp.entities;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PhraseSequenceKey implements Serializable {
    private Long userId;
    private Long phraseId;

    public PhraseSequenceKey() {}

    public PhraseSequenceKey(Long userId, Long phraseId) {
        this.userId = userId;
        this.phraseId = phraseId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPhraseId() {
        return phraseId;
    }

    public void setPhraseId(Long phraseId) {
        this.phraseId = phraseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhraseSequenceKey that = (PhraseSequenceKey) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(phraseId, that.phraseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, phraseId);
    }
}
