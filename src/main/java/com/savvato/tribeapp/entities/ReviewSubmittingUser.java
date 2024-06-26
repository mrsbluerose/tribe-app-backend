package com.savvato.tribeapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(ReviewSubmittingUserId.class)
public class ReviewSubmittingUser {
    @Id
    private Long userId;
    @Id
    private Long toBeReviewedId;

    public ReviewSubmittingUser(Long userId, Long toBeReviewedId) {
        this.userId = userId;
        this.toBeReviewedId = toBeReviewedId;
    }

    public ReviewSubmittingUser() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getToBeReviewedId() {
        return toBeReviewedId;
    }

    public void setToBeReviewedId(Long toBeReviewedId) {
        this.toBeReviewedId = toBeReviewedId;
    }
}
