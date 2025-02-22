package com.savvato.tribeapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class RejectedPhrase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String rejectedPhrase;

    public RejectedPhrase(Long id, String rejectedPhrase) {
        this.id = id;
        this.rejectedPhrase = rejectedPhrase;
    }

    public RejectedPhrase(String rejectedPhrase) {
        this.rejectedPhrase = rejectedPhrase;
    }

    public RejectedPhrase() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRejectedPhrase() {
        return rejectedPhrase;
    }

    public void setRejectedPhrase(String rejectedPhrase) {
        this.rejectedPhrase = rejectedPhrase;
    }
}
