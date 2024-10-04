package com.savvato.tribeapp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Entity
@Table(name = "phrase_sequence")
public class PhraseSequence {

    @EmbeddedId
    private PhraseSequenceKey id;

    @Column(name = "position")
    private Integer position;

    public PhraseSequence() {}

    public PhraseSequence(Long userId, Long phraseId, Integer position) {
        this.id = new PhraseSequenceKey(userId, phraseId);
        this.position = position;
    }

    public PhraseSequenceKey getId() {
        return id;
    }

    public void setId(PhraseSequenceKey id) {
        this.id = id;
    }

    public Long getUserId() {
        return id.getUserId();
    }

    public void setUserId(Long userId) {
        id.setUserId(userId);
    }

    public Long getPhraseId() {
        return id.getPhraseId();
    }

    public void setPhraseId(Long phraseId) {
        id.setPhraseId(phraseId);
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
