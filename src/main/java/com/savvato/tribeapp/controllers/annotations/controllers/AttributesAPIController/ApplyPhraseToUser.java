package com.savvato.tribeapp.controllers.annotations.controllers.AttributesAPIController;

import com.savvato.tribeapp.controllers.annotations.requests.DocumentedRequestBody;
import com.savvato.tribeapp.controllers.annotations.responses.Success;
import com.savvato.tribeapp.controllers.dto.AttributesRequest;
import com.savvato.tribeapp.dto.AttributesApplyPhraseToUserDTO;
import com.savvato.tribeapp.dto.ToBeReviewedDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import java.lang.annotation.*;

/** Documentation for applying attributes to a user */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
    summary = "Apply phrase to user",
    description = "Provided a valid AttributesRequest (see schemas), apply phrase to user.")
@DocumentedRequestBody(implementation = AttributesRequest.class)
@Success(
    description = "Successfully applied phrase", implementation = AttributesApplyPhraseToUserDTO.class)
public @interface ApplyPhraseToUser {}
