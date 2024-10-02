package com.savvato.tribeapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Result of applying phrase to user")
public class AttributesApplyPhraseToUserDTO {

    @Schema(example = "true")
    public boolean isSuccess;

    @Schema(example = "true")
    public boolean isApproved;

    @Schema(example = "true")
    public boolean isRejected;

    @Schema(example = "true")
    public boolean isInReview;

}
