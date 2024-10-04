package com.savvato.tribeapp.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhraseSequenceDataRequest {
    public Long phraseId;
    public Integer sequence;
}
