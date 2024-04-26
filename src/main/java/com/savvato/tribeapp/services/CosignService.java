package com.savvato.tribeapp.services;

import com.savvato.tribeapp.controllers.dto.CosignRequest;
import com.savvato.tribeapp.dto.CosignDTO;
import com.savvato.tribeapp.dto.CosignsForUserDTO;
import com.savvato.tribeapp.dto.GenericResponseDTO;
import com.savvato.tribeapp.dto.UsernameDTO;

import java.util.List;
import java.util.Optional;

public interface CosignService {

    Optional<CosignDTO> saveCosign(Long userIdIssuing, Long userIdReceiving, Long phraseId);

    Optional cosign(Long userIdIssuing, Long userIdReceiving, Long phraseId);

    GenericResponseDTO deleteCosign(Long userIdIssuing, Long userIdReceiving, Long phraseId);

    List<UsernameDTO> getCosignersForUserAttribute(Long userReceivingId, Long phraseId);

    List<CosignsForUserDTO> getAllCosignsForUser(Long userIdReceiving);

    Optional<GenericResponseDTO> validateCosigners(Long userIdIssuing, Long userIdReceiving);
}
