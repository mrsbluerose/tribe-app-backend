package com.savvato.tribeapp.services;

import com.savvato.tribeapp.constants.AbstractTestConstants;
import com.savvato.tribeapp.dto.CosignDTO;
import com.savvato.tribeapp.dto.CosignsForUserDTO;
import com.savvato.tribeapp.dto.GenericResponseDTO;
import com.savvato.tribeapp.dto.UsernameDTO;
import com.savvato.tribeapp.entities.Cosign;
import com.savvato.tribeapp.entities.User;
import com.savvato.tribeapp.repositories.CosignRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
public class CosignServiceImplTest extends AbstractTestConstants {

    @TestConfiguration
    static class CosignServiceImplTestContextConfiguration {

        @Bean
        public  CosignService cosignService() {
            return new CosignServiceImpl();
        }

    }

    @Autowired
    CosignService cosignService;

    @MockBean
    CosignRepository cosignRepository;

    @MockBean
    UserService userService;

    @MockBean
    GenericResponseService genericResponseService;

    @Test
    public void saveCosign() {
        Long userIdIssuing = USER1_ID;
        Long userIdReceiving = USER2_ID;
        Long phraseId = PHRASE1_ID;

        Cosign mockCosign = new Cosign();
        mockCosign.setUserIdIssuing(userIdIssuing);
        mockCosign.setUserIdReceiving(userIdReceiving);
        mockCosign.setPhraseId(phraseId);

        CosignDTO expectedCosignDTO = CosignDTO.builder().build();
        expectedCosignDTO.userIdIssuing = userIdIssuing;
        expectedCosignDTO.userIdReceiving = userIdReceiving;
        expectedCosignDTO.phraseId = phraseId;

        when(cosignRepository.save(Mockito.any())).thenReturn(mockCosign);

        Optional<CosignDTO> CosignDTO = cosignService.saveCosign(userIdIssuing, userIdReceiving, phraseId);

        verify(cosignRepository, times(1)).save(Mockito.any());
        assertThat(CosignDTO.get()).usingRecursiveComparison().isEqualTo(expectedCosignDTO);
    }

    @Test
    public void saveCosignAlreadyExisting() {
        Long userIdIssuing = USER1_ID;
        Long userIdReceiving = USER2_ID;
        Long phraseId = PHRASE1_ID;

        Cosign mockCosign = new Cosign();
        mockCosign.setUserIdIssuing(userIdIssuing);
        mockCosign.setUserIdReceiving(userIdReceiving);
        mockCosign.setPhraseId(phraseId);

        CosignDTO expectedCosignDTO = CosignDTO.builder().build();
        expectedCosignDTO.userIdIssuing = userIdIssuing;
        expectedCosignDTO.userIdReceiving = userIdReceiving;
        expectedCosignDTO.phraseId = phraseId;

        when(cosignRepository.save(Mockito.any())).thenReturn(mockCosign).thenReturn(mockCosign);

        Optional<CosignDTO> CosignDTO = cosignService.saveCosign(userIdIssuing, userIdReceiving, phraseId);

        assertThat(CosignDTO.get()).usingRecursiveComparison().isEqualTo(expectedCosignDTO);

        Optional<CosignDTO> CosignDTORepeat = cosignService.saveCosign(userIdIssuing, userIdReceiving, phraseId);

        assertThat(CosignDTORepeat.get()).usingRecursiveComparison().isEqualTo(expectedCosignDTO);
    }

    @Test
    public void testGetCosignersForUser(){
        // test data
        User testUserIssuing = getUser1();
        Long testUserIdReceiving = USER2_ID;
        Long testPhraseId = PHRASE1_ID;

        // mock return data
        UsernameDTO mockUsernameDTO = UsernameDTO.builder()
                .userId(testUserIssuing.getId())
                .username(testUserIssuing.getName())
                .build();

        List<Long> mockCosignerIds = new ArrayList<>();
        mockCosignerIds.add(testUserIssuing.getId());

        // mock returns
        when(cosignRepository.findCosignersByUserIdReceivingAndPhraseId(anyLong(),anyLong())).thenReturn(mockCosignerIds);
        when(userService.getUsernameDTO(anyLong())).thenReturn(mockUsernameDTO);

        // expected results
        List<UsernameDTO> expectedListUsernameDTO = new ArrayList<>();
        expectedListUsernameDTO.add(mockUsernameDTO);

        // test
        List<UsernameDTO> usernameDTOS = cosignService.getCosignersForUserAttribute(testUserIdReceiving,testPhraseId);

        for(UsernameDTO user : usernameDTOS){
            assertEquals(user.userId, expectedListUsernameDTO.get(0).userId);
            assertEquals(user.username, expectedListUsernameDTO.get(0).username);
        }
    }

    @Test
    public void testGetCosignersForUserWithThreeCosigners(){
        // test data
        User testUserIssuing1 = getUser1();
        User testUserIssuing2 = getUser2();
        User testUserIssuing3 = getUser3();
        Long testUserIdReceiving = 4L;
        Long testPhraseId = PHRASE1_ID;

        // mock return data
        UsernameDTO mockUsernameDTO1 = UsernameDTO.builder()
                .userId(testUserIssuing1.getId())
                .username(testUserIssuing1.getName())
                .build();

        UsernameDTO mockUsernameDTO2 = UsernameDTO.builder()
                .userId(testUserIssuing2.getId())
                .username(testUserIssuing2.getName())
                .build();

        UsernameDTO mockUsernameDTO3 = UsernameDTO.builder()
                .userId(testUserIssuing3.getId())
                .username(testUserIssuing3.getName())
                .build();

        List<Long> mockCosignerIds = new ArrayList<>();
        mockCosignerIds.add(testUserIssuing1.getId());
        mockCosignerIds.add(testUserIssuing2.getId());
        mockCosignerIds.add(testUserIssuing3.getId());

        // mock returns
        when(cosignRepository.findCosignersByUserIdReceivingAndPhraseId(anyLong(),anyLong())).thenReturn(mockCosignerIds);
        when(userService.getUsernameDTO(anyLong())).thenReturn(mockUsernameDTO1).thenReturn(mockUsernameDTO2).thenReturn(mockUsernameDTO3);

        // expected results
        List<UsernameDTO> expectedListUsernameDTO = new ArrayList<>();
        expectedListUsernameDTO.add(mockUsernameDTO1);
        expectedListUsernameDTO.add(mockUsernameDTO2);
        expectedListUsernameDTO.add(mockUsernameDTO3);

        // test
        List<UsernameDTO> usernameDTOS = cosignService.getCosignersForUserAttribute(testUserIdReceiving,testPhraseId);

        for(int i=0; i<usernameDTOS.size(); i++){
            assertEquals(usernameDTOS.get(i).userId, expectedListUsernameDTO.get(i).userId);
            assertEquals(usernameDTOS.get(i).username, expectedListUsernameDTO.get(i).username);
        }
    }

    @Test
    public void testGetAllCosignsForUser() {
        // test data
        User testUserIssuing = getUser1();
        Long testPhraseId = PHRASE1_ID;
        Long testUserIdReceiving = USER2_ID;

        // mock return data
        Cosign mockCosign = new Cosign();
        mockCosign.setUserIdIssuing(testUserIssuing.getId());
        mockCosign.setUserIdReceiving(testUserIdReceiving);
        mockCosign.setPhraseId(testPhraseId);

        List<Cosign> mockAllCosignsByUserIdReceivingList = new ArrayList<>();
        mockAllCosignsByUserIdReceivingList.add(mockCosign);

        UsernameDTO mockUsernameDTO = UsernameDTO.builder()
                .userId(testUserIssuing.getId())
                .username(testUserIssuing.getName())
                .build();

        List<UsernameDTO> mockUsernameDTOSList = new ArrayList<>();
        mockUsernameDTOSList.add(mockUsernameDTO);

        CosignsForUserDTO mockCosignsForUserDTO = CosignsForUserDTO.builder()
                .phraseId(testPhraseId)
                .listOfCosigners(mockUsernameDTOSList)
                .build();

        // mock returns
        when(cosignRepository.findAllByUserIdReceiving(anyLong())).thenReturn(mockAllCosignsByUserIdReceivingList);
        when(userService.getUsernameDTO(anyLong())).thenReturn(mockUsernameDTO);

        // expected results
        List<CosignsForUserDTO> expectedCosignsForUserDTOSList = new ArrayList<>();
        expectedCosignsForUserDTOSList.add(mockCosignsForUserDTO);

        // test
        List<CosignsForUserDTO> testCosignsForUserDTOs = cosignService.getAllCosignsForUser(testUserIdReceiving);

        for(CosignsForUserDTO testCosignsForUserDTO : testCosignsForUserDTOs) {
            assertEquals(testCosignsForUserDTO.phraseId,expectedCosignsForUserDTOSList.get(0).phraseId);
            for(UsernameDTO testUsernameDTO : testCosignsForUserDTO.listOfCosigners){
                assertEquals(testUsernameDTO.userId, expectedCosignsForUserDTOSList.get(0).listOfCosigners.get(0).userId);
                assertEquals(testUsernameDTO.username,expectedCosignsForUserDTOSList.get(0).listOfCosigners.get(0).username);
            }
        }
    }

    @Test
    public void testGetAllCosignsForUserWithThreeCosignsForThreePhrases() {
        // test data
        User testUserIssuing1 = getUser1();
        User testUserIssuing2 = getUser2();
        User testUserIssuing3 = getUser3();
        Long testPhraseId1 = PHRASE1_ID;
        Long testPhraseId2 = PHRASE2_ID;
        Long testPhraseId3 = PHRASE3_ID;
        Long testUserIdReceiving = 4L;

        // mock return data
        Long[] phrases = {PHRASE1_ID,PHRASE2_ID,PHRASE3_ID};
        User[] users = {testUserIssuing1, testUserIssuing2, testUserIssuing3};
        List<Cosign> mockAllCosignsByUserIdReceivingList = new ArrayList<>();
        for(Long phraseId : phrases) { // create 3 cosigns for each of 3 phrases (9 total)
            for(User user : users) {
                Cosign cosign = new Cosign();
                cosign.setPhraseId(phraseId);
                cosign.setUserIdIssuing(user.getId());
                cosign.setUserIdReceiving(testUserIdReceiving);
                mockAllCosignsByUserIdReceivingList.add(cosign);
            }
        }

        UsernameDTO mockUsernameDTO1 = UsernameDTO.builder()
                .userId(testUserIssuing1.getId())
                .username(testUserIssuing2.getName())
                .build();

        UsernameDTO mockUsernameDTO2 = UsernameDTO.builder()
                .userId(testUserIssuing2.getId())
                .username(testUserIssuing2.getName())
                .build();

        UsernameDTO mockUsernameDTO3 = UsernameDTO.builder()
                .userId(testUserIssuing3.getId())
                .username(testUserIssuing3.getName())
                .build();

        List<UsernameDTO> mockUsernameDTOSList1 = new ArrayList<>();
        mockUsernameDTOSList1.add(mockUsernameDTO1);
        mockUsernameDTOSList1.add(mockUsernameDTO2);
        mockUsernameDTOSList1.add(mockUsernameDTO3);

        List<UsernameDTO> mockUsernameDTOSList2 = new ArrayList<>();
        mockUsernameDTOSList2.add(mockUsernameDTO1);
        mockUsernameDTOSList2.add(mockUsernameDTO2);
        mockUsernameDTOSList2.add(mockUsernameDTO3);

        List<UsernameDTO> mockUsernameDTOSList3 = new ArrayList<>();
        mockUsernameDTOSList3.add(mockUsernameDTO1);
        mockUsernameDTOSList3.add(mockUsernameDTO2);
        mockUsernameDTOSList3.add(mockUsernameDTO3);

        CosignsForUserDTO mockCosignsForUserDTO1 = CosignsForUserDTO.builder()
                .phraseId(testPhraseId1)
                .listOfCosigners(mockUsernameDTOSList1)
                .build();

        CosignsForUserDTO mockCosignsForUserDTO2 = CosignsForUserDTO.builder()
                .phraseId(testPhraseId2)
                .listOfCosigners(mockUsernameDTOSList2)
                .build();

        CosignsForUserDTO mockCosignsForUserDTO3 = CosignsForUserDTO.builder()
                .phraseId(testPhraseId3)
                .listOfCosigners(mockUsernameDTOSList3)
                .build();

        // mock returns
        when(cosignRepository.findAllByUserIdReceiving(anyLong())).thenReturn(mockAllCosignsByUserIdReceivingList);
        when(userService.getUsernameDTO(anyLong())).thenReturn(mockUsernameDTO1).thenReturn(mockUsernameDTO2).thenReturn(mockUsernameDTO3);

        // expected results
        List<CosignsForUserDTO> expectedCosignsForUserDTOSList = new ArrayList<>();
        expectedCosignsForUserDTOSList.add(mockCosignsForUserDTO1);
        expectedCosignsForUserDTOSList.add(mockCosignsForUserDTO2);
        expectedCosignsForUserDTOSList.add(mockCosignsForUserDTO3);

        // test
        List<CosignsForUserDTO> testCosignsForUserDTOs = cosignService.getAllCosignsForUser(testUserIdReceiving);

        assertThat(expectedCosignsForUserDTOSList).usingRecursiveComparison().isEqualTo(testCosignsForUserDTOs);
    }

    @Test
    public void testValidateCosignersWhenCosignersAreValid() {
        Long loggedInUser = USER1_ID;
        Long userIdIssuing = USER1_ID;
        Long userIdReceiving = USER2_ID;

        when(userService.getLoggedInUserId()).thenReturn(loggedInUser);

        Optional<GenericResponseDTO> opt = cosignService.validateCosigners(userIdIssuing,userIdReceiving);
        assertTrue(opt.isEmpty());
        verify(userService, times(1)).getLoggedInUserId();
        verify(genericResponseService,times(0)).createDTO(anyString());
    }

    @Test
    public void testValidateCosignersWhenIssuingUserNotLoggedIn() {
        Long loggedInUser = USER3_ID;
        Long userIdIssuing = USER1_ID;
        Long userIdReceiving = USER2_ID;
        GenericResponseDTO expectedDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .responseMessage("The logged in user (" + loggedInUser + ") does not match issuing user (" + userIdIssuing + ")")
                .build();

        when(userService.getLoggedInUserId()).thenReturn(loggedInUser);

        Optional<GenericResponseDTO> opt = cosignService.validateCosigners(userIdIssuing,userIdReceiving);
        assertThat(expectedDTO).usingRecursiveComparison().isEqualTo(opt.get());
        verify(userService, times(1)).getLoggedInUserId();
    }

    @Test
    public void testValidateCosignersWhenIssuingUserCosigningSelf() {
        Long loggedInUser = USER1_ID;
        Long userIdIssuing = USER1_ID;
        Long userIdReceiving = USER1_ID;
        GenericResponseDTO expectedDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .responseMessage("User " + userIdIssuing + " may not cosign themselves.")
                .build();

        when(userService.getLoggedInUserId()).thenReturn(loggedInUser);

        Optional<GenericResponseDTO> opt = cosignService.validateCosigners(userIdIssuing,userIdReceiving);
        assertThat(expectedDTO).usingRecursiveComparison().isEqualTo(opt.get());
        verify(userService, times(1)).getLoggedInUserId();
    }

    @Test
    public void testCosignHappyPath() {
        Long userIdIssuing = USER1_ID;
        Long userIdReceiving = USER2_ID;
        Long phraseId = PHRASE1_ID;

        CosignDTO expectedCosignDTO = CosignDTO.builder()
                .userIdIssuing(userIdIssuing)
                .userIdReceiving(userIdReceiving)
                .phraseId(phraseId)
                .build();

        CosignService cosignServiceSpy = spy(cosignService);
        doReturn(Optional.empty()).when(cosignServiceSpy).validateCosigners(Mockito.any(), Mockito.any());
        doReturn(Optional.of(expectedCosignDTO)).when(cosignServiceSpy).saveCosign(Mockito.any(), Mockito.any(), Mockito.any());

        Optional opt = cosignServiceSpy.cosign(userIdIssuing,userIdReceiving,phraseId);
        assertThat(expectedCosignDTO).usingRecursiveComparison().isEqualTo(opt.get());
    }

    @Test
    public void testCosignSadPath() {
        Long userIdIssuing = USER1_ID;
        Long userIdReceiving = USER2_ID;
        Long phraseId = PHRASE1_ID;

        GenericResponseDTO expectedGenericResponseDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .responseMessage("response message")
                .build();

        CosignService cosignServiceSpy = spy(cosignService);
        doReturn(Optional.of(expectedGenericResponseDTO)).when(cosignServiceSpy).validateCosigners(Mockito.any(), Mockito.any());

        Optional opt = cosignServiceSpy.cosign(userIdIssuing,userIdReceiving,phraseId);
        assertThat(expectedGenericResponseDTO).usingRecursiveComparison().isEqualTo(opt.get());
        verify(cosignServiceSpy, times(1)).validateCosigners(anyLong(),anyLong());
        verify(cosignServiceSpy, times(0)).saveCosign(anyLong(),anyLong(),anyLong());
    }

    @Test
    public void testDeleteCosignCosignersInvalid() {

        Long userIdIssuing = USER1_ID;
        Long userIdReceiving = USER2_ID;
        Long phraseId = PHRASE1_ID;

        GenericResponseDTO expectedDTO = GenericResponseDTO.builder().build();
        expectedDTO.booleanMessage = false;
        expectedDTO.responseMessage = "message";

        CosignService cosignServiceSpy = spy(cosignService);
        doReturn(Optional.of(expectedDTO)).when(cosignServiceSpy).validateCosigners(Mockito.any(), Mockito.any());

        GenericResponseDTO actualDTO = cosignServiceSpy.deleteCosign(userIdIssuing,userIdReceiving,phraseId);
        assertThat(expectedDTO).usingRecursiveComparison().isEqualTo(actualDTO);
        verify(cosignServiceSpy, times(1)).validateCosigners(anyLong(),anyLong());
        verify(cosignRepository, never()).delete(Mockito.any());
    }

    @Test
    public void testDeleteCosignExceptionThrownOnDelete() {
        Long userIdIssuing = USER1_ID;
        Long userIdReceiving = USER2_ID;
        Long phraseID = PHRASE1_ID;

        GenericResponseDTO expectedDTO = GenericResponseDTO.builder().build();
        expectedDTO.booleanMessage = false;
        expectedDTO.responseMessage = "Database delete failed.";

        CosignService cosignServiceSpy = spy(cosignService);
        doReturn(Optional.empty()).when(cosignServiceSpy).validateCosigners(Mockito.any(), Mockito.any());
        doThrow(new IllegalArgumentException("Database delete failed.")).when(cosignRepository).delete(Mockito.any());

        GenericResponseDTO actualDTO = cosignServiceSpy.deleteCosign(userIdIssuing,userIdReceiving,phraseID);
        assertThat(expectedDTO).usingRecursiveComparison().isEqualTo(actualDTO);
        verify(cosignServiceSpy, times(1)).validateCosigners(anyLong(),anyLong());
        verify(cosignRepository, times(1)).delete(Mockito.any());
    }

    @Test
    public void testDeleteCosignHappyPath() {
        Long userIdIssuing = USER1_ID;
        Long userIdReceiving = USER2_ID;
        Long phraseID = PHRASE1_ID;

        GenericResponseDTO expectedDTO = GenericResponseDTO.builder().build();
        expectedDTO.booleanMessage = true;

        CosignService cosignServiceSpy = spy(cosignService);
        doReturn(Optional.empty()).when(cosignServiceSpy).validateCosigners(Mockito.any(), Mockito.any());
        doNothing().when(cosignRepository).delete(Mockito.any());

        GenericResponseDTO actualDTO = cosignServiceSpy.deleteCosign(userIdIssuing,userIdReceiving,phraseID);
        assertThat(expectedDTO).usingRecursiveComparison().isEqualTo(actualDTO);
        verify(cosignServiceSpy, times(1)).validateCosigners(anyLong(),anyLong());
        verify(cosignRepository, times(1)).delete(Mockito.any());
    }

}
