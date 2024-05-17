package com.savvato.tribeapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UsernameDTO {
  @Schema(example = "1")
  public Long userId;

  @Schema(example = "John Doe")
  public String username;

}
