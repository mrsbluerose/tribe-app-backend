package com.savvato.tribeapp.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhraseSequenceRequest {
    public Long userId;
    public List<PhraseSequenceDataRequest> phrases;

}

