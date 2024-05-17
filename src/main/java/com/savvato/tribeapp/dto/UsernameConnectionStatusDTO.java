package com.savvato.tribeapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UsernameConnectionStatusDTO extends UsernameDTO {

    @Schema(example = "receiving user")
    public String userConnectionStatus;

}