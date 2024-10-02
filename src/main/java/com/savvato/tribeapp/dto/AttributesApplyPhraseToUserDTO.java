package com.savvato.tribeapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Result of applying phrase to user")
public class AttributesApplyPhraseToUserDTO {

    @Schema(example = "true")
    public boolean success;

    @Schema(example = "true")
    public boolean approved;

    @Schema(example = "true")
    public boolean rejected;

    @Schema(example = "true")
    public boolean inReview;

}
