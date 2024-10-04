package com.savvato.tribeapp.dto;

import com.savvato.tribeapp.entities.PhraseSequence;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

// hold when data is going out

@Schema(description = "An attribute DTO")
@Builder
public class AttributeDTO {
    public PhraseDTO phrase;
    @Schema(example = "1")
    public Integer userCount;
    public int sequence;
}
