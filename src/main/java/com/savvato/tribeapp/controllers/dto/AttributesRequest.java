package com.savvato.tribeapp.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
// hold when data is coming in to the request
@Schema(description = "An attributes request")
public class AttributesRequest {

  @Schema(example = "1")
  public Long userId;

  @Schema(example = "1")
  public Long id;

  @Schema(example = "enthusiastically")
  public String adverb;

  @Schema(example = "volunteers")
  public String verb;

  @Schema(example = "at")
  public String preposition;

  @Schema(example = "UNICEF")
  public String noun;

  public List<PhraseSequenceDataRequest> phrases;

}
