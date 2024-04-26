package com.savvato.tribeapp.services;

import com.savvato.tribeapp.controllers.dto.CosignRequest;
import com.savvato.tribeapp.dto.CosignDTO;
import com.savvato.tribeapp.dto.CosignsForUserDTO;
import com.savvato.tribeapp.dto.GenericResponseDTO;
import com.savvato.tribeapp.dto.UsernameDTO;
import com.savvato.tribeapp.entities.Cosign;
import com.savvato.tribeapp.repositories.CosignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CosignServiceImpl implements CosignService {

    @Autowired
    CosignRepository cosignRepository;

    @Autowired
    UserService userService;

    @Autowired
    GenericResponseService genericResponseService;

    @Override
    public Optional<CosignDTO> saveCosign(Long userIdIssuing, Long userIdReceiving, Long phraseId) {

        Cosign cosign = new Cosign();
        cosign.setUserIdIssuing(userIdIssuing);
        cosign.setUserIdReceiving(userIdReceiving);
        cosign.setPhraseId(phraseId);

        Cosign savedCosign = cosignRepository.save(cosign);
        log.info("Cosign from user " + userIdIssuing + " to user " + userIdReceiving + " added." );

        CosignDTO cosignDTO = CosignDTO
                .builder()
                .userIdIssuing(savedCosign.getUserIdIssuing())
                .userIdReceiving(savedCosign.getUserIdReceiving())
                .phraseId(savedCosign.getPhraseId())
                .build();

        return Optional.of(cosignDTO);
    }

    @Override
    public Optional cosign(CosignRequest cosignRequest) {

        Optional<GenericResponseDTO> optValidate = validateCosigners(cosignRequest.userIdIssuing,cosignRequest.userIdReceiving);

        if(optValidate.isPresent()) {
            optValidate.get().booleanMessage = false;
            return optValidate;
        }

        return saveCosign(cosignRequest.userIdIssuing,cosignRequest.userIdReceiving,cosignRequest.phraseId);

    }

    @Override
    public GenericResponseDTO deleteCosign(Long userIdIssuing, Long userIdReceiving, Long phraseId) {

        Optional<GenericResponseDTO> optValidateDTO = validateCosigners(userIdIssuing,userIdReceiving);

        if(optValidateDTO.isPresent()) {
            return optValidateDTO.get();
        }

        GenericResponseDTO rtn = GenericResponseDTO.builder().build();

        try {
            Cosign cosign = new Cosign();
            cosign.userIdIssuing = userIdIssuing;
            cosign.userIdReceiving = userIdReceiving;
            cosign.phraseId = phraseId;
            cosignRepository.delete(cosign);
            rtn.booleanMessage = true;
        } catch (Exception e) {
            rtn.booleanMessage = false;
            rtn.responseMessage = e.getMessage();
        }

        return rtn;
    }

    @Override
    public List<UsernameDTO> getCosignersForUserAttribute(Long userIdReceiving, Long phraseId) {

        List<UsernameDTO> list = new ArrayList<>();
        List<Long> cosignerIds = cosignRepository.findCosignersByUserIdReceivingAndPhraseId(userIdReceiving, phraseId);

        for(Long id : cosignerIds) {
            list.add(userService.getUsernameDTO(id));
        }

        return list;
    }

    @Override
    public List<CosignsForUserDTO> getAllCosignsForUser(Long userIdReceiving) {

        List<CosignsForUserDTO> cosignsForUserDTOs = new ArrayList<>();

        List<Cosign> allCosignsByUserIdReceiving = cosignRepository.findAllByUserIdReceiving(userIdReceiving);
        Map<Long, List<UsernameDTO>> mapOfPhrasesAndUserIdsIssuing = new HashMap<>();
        Map<Long, UsernameDTO> mapOfUsernameDTOs = new HashMap<>();

        for(Cosign cosign : allCosignsByUserIdReceiving) {
            Long userIdIssuing = cosign.getUserIdIssuing();
            Long phraseId = cosign.getPhraseId();

            if(!mapOfUsernameDTOs.containsKey(userIdIssuing)) {
                mapOfUsernameDTOs.put(userIdIssuing,userService.getUsernameDTO(userIdIssuing));
            }

            if(mapOfPhrasesAndUserIdsIssuing.containsKey(phraseId)){
                mapOfPhrasesAndUserIdsIssuing.get(phraseId).add(mapOfUsernameDTOs.get(userIdIssuing));
            } else {
                List<UsernameDTO> list = new ArrayList<>();
                list.add(mapOfUsernameDTOs.get(userIdIssuing));
                mapOfPhrasesAndUserIdsIssuing.put(phraseId,list);
            }
        }

        mapOfPhrasesAndUserIdsIssuing.forEach((k,v) -> {
            CosignsForUserDTO cosignsForUserDTO = CosignsForUserDTO.builder()
                    .phraseId(k)
                    .listOfCosigners(v)
                    .build();
            cosignsForUserDTOs.add(cosignsForUserDTO);
        });

        return cosignsForUserDTOs;
    }

    @Override
    public Optional<GenericResponseDTO> validateCosigners(Long userIdIssuing, Long userIdReceiving) {

        GenericResponseDTO rtnDTO = GenericResponseDTO.builder().build();
        Long loggedInUser = userService.getLoggedInUserId();

        if (!loggedInUser.equals(userIdIssuing)) {
            rtnDTO.booleanMessage = false;
            rtnDTO.responseMessage = "The logged in user (" + loggedInUser + ") does not match issuing user (" + userIdIssuing + ")";
            return Optional.of(rtnDTO);
        }

        if (userIdIssuing.equals(userIdReceiving)) {
            rtnDTO.booleanMessage = false;
            rtnDTO.responseMessage = "User " + userIdIssuing + " may not cosign themselves.";
            return Optional.of(rtnDTO);
        }

            return Optional.empty();
    }
}
