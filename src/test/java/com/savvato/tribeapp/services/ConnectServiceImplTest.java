package com.savvato.tribeapp.services;

import com.savvato.tribeapp.config.principal.UserPrincipal;
import com.savvato.tribeapp.controllers.dto.ConnectRequest;
import com.savvato.tribeapp.controllers.dto.ConnectionRemovalRequest;
import com.savvato.tribeapp.dto.ConnectOutgoingMessageDTO;
import com.savvato.tribeapp.dto.GenericResponseDTO;
import com.savvato.tribeapp.dto.UsernameConnectionStatusDTO;
import com.savvato.tribeapp.entities.Connection;
import com.savvato.tribeapp.repositories.ConnectionsRepository;
import com.savvato.tribeapp.repositories.UserRepository;
import liquibase.pro.packaged.U;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.savvato.tribeapp.constants.Constants;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
public class ConnectServiceImplTest extends AbstractServiceImplTest {
    @TestConfiguration
    static class ConnectServiceImplTestContextConfiguration {
        @Bean
        public ConnectService connectService() {
            return new ConnectServiceImpl();
        }

        @Bean
        public CacheService cacheService() {
            return new CacheServiceImpl();
        }
    }

    @Autowired
    ConnectService connectService;

    @MockBean
    CacheService cacheService;

    @MockBean
    ConnectionsRepository connectionsRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    UserService userService;

    @MockBean
    GenericResponseService genericResponseService;

    @Test
    public void getQRCodeString() {
        Long userId = USER1_ID;
        Optional<String> qrCodeString = Optional.of("QR code");
        Mockito.when(cacheService.get(Mockito.any(), Mockito.any())).thenReturn(qrCodeString.get());
        Optional<String> rtn = connectService.getQRCodeString(userId);
        assertEquals(qrCodeString, rtn);
    }

    @Test
    public void storeQRCodeString() {
        Long userId = USER1_ID;
        Optional<String> generatedQRCodeString = Optional.of("QR code");

        Optional<String> rtn = connectService.storeQRCodeString(userId);

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
        verify(cacheService, times(1)).put(arg1.capture(), arg2.capture(), Mockito.any());

        assertEquals(arg1.getValue(), "ConnectQRCodeString");
        assertEquals(arg2.getValue(), String.valueOf(userId));
    }

    @Test
    public void saveConnectionDetailsHappyPath() {
        Long requestingUserId = USER1_ID;
        Long toBeConnectedWithUserId = USER2_ID;

        Boolean connectionStatus = connectService.saveConnectionDetails(requestingUserId, toBeConnectedWithUserId);
        assertEquals(connectionStatus, true);
    }

    @Test
    public void saveConnectionDetailsUnhappyPath() {
        Long requestingUserId = USER1_ID;
        Long toBeConnectedWithUserId = USER2_ID;
        doThrow(new IndexOutOfBoundsException()).when(connectionsRepository).save(Mockito.any());
        Boolean connectionStatus = connectService.saveConnectionDetails(requestingUserId, toBeConnectedWithUserId);
        assertEquals(connectionStatus, false);
    }

    @Test
    public void testValidateConnectionHappyPath() {
        Long requestingUserId = USER1_ID;
        Long toBeConnectedWithUserId = USER2_ID;

        when(userService.getLoggedInUserId()).thenReturn(USER1_ID);

        Optional<GenericResponseDTO> validateConnection = connectService.validateConnection(requestingUserId, toBeConnectedWithUserId);

        assertThat(validateConnection.isEmpty());
        verify(userService, times(1)).getLoggedInUserId();

    }

    @Test
    public void testValidateConnectionWhenIdsAreTheSame() {
        Long requestingUserId = USER2_ID;
        Long toBeConnectedWithUserId = USER2_ID;
        GenericResponseDTO expectedGenericResponseDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .responseMessage("User " + requestingUserId + " may not have a connection to themselves")
                .build();

        when(userService.getLoggedInUserId()).thenReturn(USER2_ID);

        Optional<GenericResponseDTO> validateConnection = connectService.validateConnection(requestingUserId, toBeConnectedWithUserId);

        assertThat(expectedGenericResponseDTO).usingRecursiveComparison().isEqualTo(validateConnection.get());
        verify(userService, times(1)).getLoggedInUserId();

    }

    @Test
    public void testValidateConnectionWhenRequestingUserNotLoggedIn() {
        Long loggedInUser = 3L;
        Long requestingUserId = USER1_ID;
        Long toBeConnectedWithUserId = USER2_ID;
        GenericResponseDTO expectedGenericResponseDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .responseMessage("The logged in user (" + loggedInUser + ") does not match requesting user (" + requestingUserId + ")")
                .build();

        when(userService.getLoggedInUserId()).thenReturn(loggedInUser);

        Optional<GenericResponseDTO> validateConnection = connectService.validateConnection(requestingUserId,toBeConnectedWithUserId);

        assertThat(expectedGenericResponseDTO).usingRecursiveComparison().isEqualTo(validateConnection.get());
        verify(userService, times(1)).getLoggedInUserId();

    }

    @Test
    public void validateQRCode() {
        ConnectService connectServiceSpy = spy(connectService);
        Optional<String> qrCodeOpt = Optional.of("ABCDEFGHIJKL");
        String providedQRCode = "ABCDEFGHIJKL";
        Long toBeConnectedWithUserId = USER1_ID;
        doReturn(qrCodeOpt).when(connectServiceSpy).getQRCodeString(anyLong());
        Boolean isValid = connectServiceSpy.validateQRCode(providedQRCode, toBeConnectedWithUserId);
        assertTrue(isValid);
    }

    @Test
    public void removeConnectionHappyPath() {
        ConnectionRemovalRequest connectionDeleteRequest = new ConnectionRemovalRequest();
        connectionDeleteRequest.requestingUserId = USER1_ID;
        connectionDeleteRequest.connectedWithUserId = USER2_ID;

        GenericResponseDTO expectedDTO = GenericResponseDTO.builder()
                .booleanMessage(true)
                .build();

        ConnectService connectServiceSpy = spy(connectService);
        doReturn(Optional.empty()).when(connectServiceSpy).validateConnection(Mockito.any(),Mockito.any());
        doNothing().when(connectionsRepository).removeConnection(anyLong(), anyLong());

        ArgumentCaptor<Long> requestingUserIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> connectedWithUserIdCaptor = ArgumentCaptor.forClass(Long.class);

        GenericResponseDTO actualDTO = connectServiceSpy.removeConnection(connectionDeleteRequest);

        assertThat(expectedDTO).usingRecursiveComparison().isEqualTo(actualDTO);
        verify(connectionsRepository, times(1)).removeConnection(requestingUserIdCaptor.capture(), connectedWithUserIdCaptor.capture());
        assertEquals(requestingUserIdCaptor.getValue(), connectionDeleteRequest.requestingUserId);
        assertEquals(connectedWithUserIdCaptor.getValue(), connectionDeleteRequest.connectedWithUserId);
    }

    @Test
    public void removeConnectionWhenDatabaseDeleteFails() {
        ConnectionRemovalRequest connectionDeleteRequest = new ConnectionRemovalRequest();
        connectionDeleteRequest.requestingUserId = USER1_ID;
        connectionDeleteRequest.connectedWithUserId = USER2_ID;

        GenericResponseDTO expectedDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .build();

        ConnectService connectServiceSpy = spy(connectService);
        doReturn(Optional.empty()).when(connectServiceSpy).validateConnection(Mockito.any(),Mockito.any());

        doThrow(new IllegalArgumentException("Database delete failed.")).when(connectionsRepository).removeConnection(anyLong(), anyLong());

        ArgumentCaptor<Long> requestingUserIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> connectedWithUserIdCaptor = ArgumentCaptor.forClass(Long.class);

        GenericResponseDTO actualDTO = connectServiceSpy.removeConnection(connectionDeleteRequest);

        assertThat(expectedDTO).usingRecursiveComparison().isEqualTo(actualDTO);
        verify(connectionsRepository, times(1)).removeConnection(requestingUserIdCaptor.capture(), connectedWithUserIdCaptor.capture());
        assertEquals(requestingUserIdCaptor.getValue(), connectionDeleteRequest.requestingUserId);
        assertEquals(connectedWithUserIdCaptor.getValue(), connectionDeleteRequest.connectedWithUserId);
    }

    @Test
    public void removeConnectionWhenConnectionInvalid() {
        ConnectionRemovalRequest connectionRemovalRequest = new ConnectionRemovalRequest();
        connectionRemovalRequest.requestingUserId = USER1_ID;
        connectionRemovalRequest.connectedWithUserId = USER1_ID;

        GenericResponseDTO expectedDTO = GenericResponseDTO.builder()
                .responseMessage("message")
                .booleanMessage(false)
                .build();

        ConnectService connectServiceSpy = spy(connectService);
        doReturn(Optional.of(expectedDTO)).when(connectServiceSpy).validateConnection(Mockito.any(),Mockito.any());

        GenericResponseDTO actualDTO = connectServiceSpy.removeConnection(connectionRemovalRequest);

        assertThat(expectedDTO).usingRecursiveComparison().isEqualTo(actualDTO);
        verify(connectionsRepository, never()).removeConnection(anyLong(), anyLong());
    }

    @Test
    public void testGetAllConnectionsForAUserWhenConnectionsExist() {
        // mock data
        Long user1 = USER1_ID;
        Long user2 = USER2_ID;

        Connection whenUser1AsToBeConnectedWith = new Connection();
        whenUser1AsToBeConnectedWith.setCreated();
        whenUser1AsToBeConnectedWith.setRequestingUserId(user2);
        whenUser1AsToBeConnectedWith.setToBeConnectedWithUserId(user1);

        Connection whenUser1AsRequesting = new Connection();
        whenUser1AsRequesting.setCreated();
        whenUser1AsRequesting.setRequestingUserId(user1);
        whenUser1AsRequesting.setToBeConnectedWithUserId(user2);

        List<ConnectOutgoingMessageDTO> expectedOutgoingMessageDTOS = new ArrayList<>();

        ConnectOutgoingMessageDTO outgoingMessageWhenUser1IsToBeConnectedWith = ConnectOutgoingMessageDTO.builder()
                .connectionSuccess(true)
                .to(UsernameConnectionStatusDTO.builder()
                        .userId(user2)
                        .username(USER2_NAME)
                        .userConnectionStatus(Constants.REQUESTING_USER)
                        .build())
                .message("")
                .build();
        expectedOutgoingMessageDTOS.add(outgoingMessageWhenUser1IsToBeConnectedWith);

        ConnectOutgoingMessageDTO outGoingMessageWhenUser1IsRequesting = ConnectOutgoingMessageDTO.builder()
                .connectionSuccess(true)
                .to(UsernameConnectionStatusDTO.builder()
                        .userId(user2)
                        .username(USER2_NAME)
                        .userConnectionStatus(Constants.TO_BE_CONNECTED_WITH_USER)
                        .build())
                .message("")
                .build();
        expectedOutgoingMessageDTOS.add(outGoingMessageWhenUser1IsRequesting);

        // mock returns
        when(connectionsRepository.findAllByToBeConnectedWithUserId(anyLong())).thenReturn(List.of(whenUser1AsToBeConnectedWith));
        when(connectionsRepository.findAllByRequestingUserId(anyLong())).thenReturn(List.of(whenUser1AsRequesting));
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(getUser2())).thenReturn(Optional.of(getUser2()));

        // test
        List<ConnectOutgoingMessageDTO> actualMessageDTOs = connectService.getAllConnectionsForAUser(user1);

        assertThat(actualMessageDTOs).usingRecursiveComparison().isEqualTo(expectedOutgoingMessageDTOS);

    }

    @Test
    public void testGetAllConnectionsForAUserWhenConnectionsDoNotExist() {
        Long User1 = USER1_ID;

        when(connectionsRepository.findAllByToBeConnectedWithUserId(anyLong())).thenReturn(Collections.emptyList());
        when(connectionsRepository.findAllByRequestingUserId(anyLong())).thenReturn(Collections.emptyList());

        List<ConnectOutgoingMessageDTO> actualMessageDTOs = connectService.getAllConnectionsForAUser(User1);

        assertThat(actualMessageDTOs).usingRecursiveComparison().isEqualTo(Collections.emptyList());
        verify(connectionsRepository, times(1)).findAllByToBeConnectedWithUserId(anyLong());
        verify(connectionsRepository, times(1)).findAllByRequestingUserId(anyLong());
        verify(userRepository, times(0)).findById(anyLong());
    }

    @Test
    public void validateQRCodeWhenQRCodeIsEmpty() {
        String qrcodePhrase = "";
        Long userId = USER1_ID;
        when(cacheService.get(any(), any())).thenReturn("");
        boolean isValidQRCode = connectService.validateQRCode(qrcodePhrase, userId);
        assertFalse(isValidQRCode);
    }

    @Test
    public void validateQRCodeWhenQRCodeIsValid() {
        String qrcodePhrase = "ABCDE";
        Long userId = USER1_ID;
        when(cacheService.get(any(), any())).thenReturn(qrcodePhrase);
        boolean isValidQRCode = connectService.validateQRCode(qrcodePhrase, userId);
        assertTrue(isValidQRCode);
    }

    @Test
    public void validateQRCodeWhenNoQRCodeIsCached() {
        String qrcodePhrase = "ABCDE";
        Long userId = USER1_ID;
        when(cacheService.get(any(), any())).thenReturn(null);
        boolean isValidQRCode = connectService.validateQRCode(qrcodePhrase, userId);
        assertFalse(isValidQRCode);
    }

    @Test
    public void testConnectInvalidQRcode() {

        GenericResponseDTO genericResponseDTO = GenericResponseDTO.builder()
            .booleanMessage(false)
            .responseMessage("Unable to validate QR code.")
                .build();

        ConnectRequest connectRequest = new ConnectRequest();
        connectRequest.requestingUserId = USER1_ID;
        connectRequest.toBeConnectedWithUserId = USER2_ID;
        connectRequest.qrcodePhrase = "test";

        ConnectService connectServiceSpy = spy(connectService);
        doReturn(false).when(connectServiceSpy).validateQRCode(Mockito.any(),Mockito.any());

        GenericResponseDTO result = connectServiceSpy.connect(connectRequest);

        assertThat(genericResponseDTO).usingRecursiveComparison().isEqualTo(result);
        verify(connectServiceSpy, never()).validateConnection(anyLong(),anyLong());
        verify(connectionsRepository, never()).findExistingConnectionWithReversedUserIds(anyLong(),anyLong());
        verify(connectServiceSpy,never()).saveConnectionDetails(anyLong(),anyLong());
    }

    @Test
    public void testConnectInvalidConnection(){
        GenericResponseDTO genericResponseDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .responseMessage("message")
                .build();

        ConnectRequest connectRequest = new ConnectRequest();
        connectRequest.requestingUserId = USER1_ID;
        connectRequest.toBeConnectedWithUserId = USER2_ID;
        connectRequest.qrcodePhrase = "test";

        ConnectService connectServiceSpy = spy(connectService);
        doReturn(true).when(connectServiceSpy).validateQRCode(Mockito.any(),Mockito.any());
        doReturn(Optional.of(genericResponseDTO)).when(connectServiceSpy).validateConnection(Mockito.any(),Mockito.any());

        GenericResponseDTO result = connectServiceSpy.connect(connectRequest);

        assertThat(genericResponseDTO).usingRecursiveComparison().isEqualTo(result);
        verify(connectionsRepository, never()).findExistingConnectionWithReversedUserIds(anyLong(),anyLong());
        verify(connectServiceSpy,never()).saveConnectionDetails(anyLong(),anyLong());
    }

    @Test
    public void testConnectExistingConnectionWithReverseIds(){
        ConnectRequest connectRequest = new ConnectRequest();
        connectRequest.requestingUserId = USER1_ID;
        connectRequest.toBeConnectedWithUserId = USER2_ID;
        connectRequest.qrcodePhrase = "test";

        Connection existingConnection = new Connection();
        existingConnection.setRequestingUserId(USER2_ID);
        existingConnection.setToBeConnectedWithUserId((USER1_ID));

        GenericResponseDTO genericResponseDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .responseMessage("This connection already exists in reverse between the requesting user " + connectRequest.requestingUserId + " and the to be connected with user " + connectRequest.toBeConnectedWithUserId)
                .build();

        ConnectService connectServiceSpy = spy(connectService);
        doReturn(true).when(connectServiceSpy).validateQRCode(Mockito.any(),Mockito.any());
        doReturn(Optional.empty()).when(connectServiceSpy).validateConnection(Mockito.any(),Mockito.any());
        when(connectionsRepository.findExistingConnectionWithReversedUserIds(anyLong(),anyLong())).thenReturn(Optional.of(existingConnection));

        GenericResponseDTO result = connectServiceSpy.connect(connectRequest);

        assertThat(genericResponseDTO).usingRecursiveComparison().isEqualTo(result);
        verify(connectServiceSpy,never()).saveConnectionDetails(anyLong(),anyLong());
    }

    @Test
    public void testConnectHappyPath(){

        GenericResponseDTO genericResponseDTO = GenericResponseDTO.builder()
                .booleanMessage(true)
                .build();

        ConnectRequest connectRequest = new ConnectRequest();
        connectRequest.requestingUserId = USER1_ID;
        connectRequest.toBeConnectedWithUserId = USER2_ID;
        connectRequest.qrcodePhrase = "test";

        ConnectService connectServiceSpy = spy(connectService);
        doReturn(true).when(connectServiceSpy).validateQRCode(Mockito.any(),Mockito.any());
        doReturn(Optional.empty()).when(connectServiceSpy).validateConnection(Mockito.any(),Mockito.any());
        when(connectionsRepository.findExistingConnectionWithReversedUserIds(anyLong(),anyLong())).thenReturn(Optional.empty());

        GenericResponseDTO result = connectServiceSpy.connect(connectRequest);

        assertThat(genericResponseDTO).usingRecursiveComparison().isEqualTo(result);

    }
}
