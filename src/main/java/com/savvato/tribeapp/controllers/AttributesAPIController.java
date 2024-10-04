package com.savvato.tribeapp.controllers;

import com.savvato.tribeapp.controllers.annotations.controllers.AttributesAPIController.ApplyPhraseToUser;
import com.savvato.tribeapp.controllers.annotations.controllers.AttributesAPIController.DeletePhraseFromUser;
import com.savvato.tribeapp.controllers.annotations.controllers.AttributesAPIController.GetAttributesForUser;
import com.savvato.tribeapp.controllers.annotations.controllers.AttributesAPIController.GetUserPhrasesToBeReviewed;
import com.savvato.tribeapp.controllers.dto.AttributesRequest;
import com.savvato.tribeapp.controllers.dto.PhraseSequenceRequest;
import com.savvato.tribeapp.dto.AttributeDTO;
import com.savvato.tribeapp.dto.AttributesApplyPhraseToUserDTO;
import com.savvato.tribeapp.dto.GenericResponseDTO;
import com.savvato.tribeapp.dto.ToBeReviewedDTO;
import com.savvato.tribeapp.entities.NotificationType;
import com.savvato.tribeapp.services.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attributes")
@Tag(
        name = "attributes",
        description = "Everything about attributes, e.g. \"plays chess competitively\"")
public class AttributesAPIController {

    @Autowired
    AttributesService attributesService;
      
    @Autowired 
    private GenericResponseService GenericResponseService;

    @Autowired
    PhraseService phraseService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    UserPhraseService userPhraseService;

    @Autowired
    ReviewSubmittingUserService reviewSubmittingUserService;

    AttributesAPIController() {
    }

    //modify to send seq

    @GetAttributesForUser
    @GetMapping("/{userId}")
    public ResponseEntity<List<AttributeDTO>> getAttributesForUser(
            @Parameter(description = "User ID of user", example = "1") @PathVariable Long userId) {

        Optional<List<AttributeDTO>> opt = attributesService.getAttributesByUserId(userId);

        if (opt.isPresent()) return ResponseEntity.status(HttpStatus.OK).body(opt.get());
        else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
        

    @PostMapping("/update")
    public ResponseEntity<GenericResponseDTO> uPhraseSequences(@RequestBody @Valid PhraseSequenceRequest req) {
        PhraseSequenceRequest newRequest = new PhraseSequenceRequest(req.userId, req.phrases);

        // Create a request object that includes userId and the list of PhraseSequenceDataRequest
        boolean success = attributesService.loadSequence(newRequest);

        if (success){
            GenericResponseDTO rtn = GenericResponseService.createDTO(true);
            return ResponseEntity.status(HttpStatus.OK).body(rtn);
        } else {
            return ResponseEntity.status(500).build();
        }
    }

    @GetUserPhrasesToBeReviewed
    @GetMapping("/in-review/{userId}")
    public ResponseEntity<List<ToBeReviewedDTO>> getUserPhrasesToBeReviewed(
            @Parameter(description = "User ID of user", example = "1")
            @PathVariable Long userId) {
        List<ToBeReviewedDTO> rtn = reviewSubmittingUserService.getUserPhrasesToBeReviewed(userId);
        return ResponseEntity.status(HttpStatus.OK).body(rtn);
    }

    @ApplyPhraseToUser
    @PostMapping
    public ResponseEntity<AttributesApplyPhraseToUserDTO> applyPhraseToUser(@RequestBody @Valid AttributesRequest req) {

        if (!phraseService.isPhraseValid(req.adverb, req.verb, req.preposition, req.noun)) {
          AttributesApplyPhraseToUserDTO rtn = phraseService.constructAttributesApplyPhraseToUserDTO(false, false, true, false);
          sendNotification(rtn, req.userId);
          return ResponseEntity.status(HttpStatus.OK).body(rtn);
        }

      AttributesApplyPhraseToUserDTO rtn = phraseService.applyPhraseToUser(req.userId, req.adverb, req.verb, req.preposition, req.noun);
      sendNotification(rtn, req.userId);

      return ResponseEntity.status(HttpStatus.OK).body(rtn);
    }

    ///api/attributes/?phraseId=xx&userId=xx
    @DeletePhraseFromUser
    @DeleteMapping
    public ResponseEntity deletePhraseFromUser(@Parameter(description = "Phrase ID of phrase", example = "1") @RequestParam("phraseId") Long phraseId, @Parameter(description = "User ID of user", example = "1") @RequestParam("userId") Long userId) {
        userPhraseService.deletePhraseFromUser(phraseId, userId);
        return ResponseEntity.ok().build();
    }

    private void sendNotification(AttributesApplyPhraseToUserDTO dto, Long userId) {
        if (dto.isSuccess && dto.isApproved) {
            notificationService.createNotification(
                    NotificationType.ATTRIBUTE_REQUEST_APPROVED,
                    userId,
                    NotificationType.ATTRIBUTE_REQUEST_APPROVED.getName(),
                    "Your attribute has been approved!");

        } else if (dto.isInReview) {
            notificationService.createNotification(NotificationType.ATTRIBUTE_REQUEST_IN_REVIEW, userId, NotificationType.ATTRIBUTE_REQUEST_IN_REVIEW.getName(), "Your attribute will be reviewed.");

        } else if (dto.isRejected) {
            notificationService.createNotification(
                    NotificationType.ATTRIBUTE_REQUEST_REJECTED,
                    userId,
                    NotificationType.ATTRIBUTE_REQUEST_REJECTED.getName(),
                    "Your attribute was rejected. This attribute is unsuitable and cannot be applied to users.");
        }
    }
}
