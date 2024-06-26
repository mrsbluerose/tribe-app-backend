package com.savvato.tribeapp.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.savvato.tribeapp.config.SecurityConfig;
import com.savvato.tribeapp.config.principal.UserPrincipal;
import com.savvato.tribeapp.constants.PhraseTestConstants;
import com.savvato.tribeapp.constants.UserTestConstants;
import com.savvato.tribeapp.controllers.dto.ConnectRequest;
import com.savvato.tribeapp.controllers.dto.ConnectionRemovalRequest;
import com.savvato.tribeapp.controllers.dto.CosignRequest;
import com.savvato.tribeapp.dto.*;
import com.savvato.tribeapp.entities.User;
import com.savvato.tribeapp.repositories.CosignRepository;
import com.savvato.tribeapp.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Type;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConnectAPIController.class)
@Import(SecurityConfig.class)
public class ConnectAPITest implements UserTestConstants, PhraseTestConstants {
    private User user;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private Gson gson;

    @MockBean
    private UserDetailsServiceTRIBEAPP userDetailsServiceTRIBEAPP;

    @MockBean
    private UserPrincipalService userPrincipalService;

    @MockBean
    private ConnectService connectService;

    @MockBean CosignService cosignService;

    @MockBean
    private CosignRepository repository;

    @MockBean
    private GenericResponseService genericResponseService;

    @Captor
    private ArgumentCaptor<Long> userIdCaptor;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                        .apply(springSecurity())
                        .build();

        user = UserTestConstants.getUser3();

    }

    @Test
    public void getQrCodeStringHappyPath() throws Exception {
        Long userId = USER1_ID;
        String qrCode = "ABCDEFGHIJKL";

        when(connectService.storeQRCodeString(anyLong())).thenReturn(Optional.of(qrCode));
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);
        this.mockMvc
                .perform(
                        get("/api/connect/{userId}", userId)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().string(qrCode));

        verify(connectService, times(1)).storeQRCodeString(userIdCaptor.capture());
        assertEquals(userIdCaptor.getValue(), userId);
    }

    @Test
    public void getQrCodeStringWhenQrCodeNotGenerated() throws Exception {

        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);
        Long userId = USER1_ID;
        when(connectService.storeQRCodeString(anyLong())).thenReturn(Optional.empty());

        this.mockMvc
                .perform(
                        get("/api/connect/{userId}", userId)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").doesNotExist());

        verify(connectService, times(1)).storeQRCodeString(userIdCaptor.capture());
        assertEquals(userIdCaptor.getValue(), userId);
    }

    @Test
    public void connectHappyPath() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);
        ConnectRequest connectRequest = new ConnectRequest();
        GenericResponseDTO expectedGenericResponseDTO = GenericResponseDTO.builder()
                .booleanMessage(true)
                .build();

        connectRequest.requestingUserId = USER1_ID;
        connectRequest.toBeConnectedWithUserId = USER2_ID;
        connectRequest.qrcodePhrase = "ABCDEFGHIJKL";

        when(connectService.connect(anyLong(), anyLong(), anyString())).thenReturn(expectedGenericResponseDTO);

        ArgumentCaptor<Long> requestingUserIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> toBeConnectedWithUserIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> qrcodePhraseArgumentCaptor = ArgumentCaptor.forClass(String.class);

        String template = "{\"booleanMessage\": %b}";
        String expectedMessage = String.format(template, true);

        this.mockMvc
                .perform(
                        post("/api/connect")
                                .content(gson.toJson(connectRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedMessage))
                .andReturn();
        verify(connectService, times(1)).connect(requestingUserIdArgumentCaptor.capture(), toBeConnectedWithUserIdArgumentCaptor.capture(), qrcodePhraseArgumentCaptor.capture());
        assertThat(requestingUserIdArgumentCaptor.getValue()).isEqualTo(connectRequest.requestingUserId);
        assertThat(toBeConnectedWithUserIdArgumentCaptor.getValue()).isEqualTo(connectRequest.toBeConnectedWithUserId);
        assertThat(qrcodePhraseArgumentCaptor.getValue()).isEqualTo(connectRequest.qrcodePhrase);
    }

    @Test
    public void connectSadPath() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);
        ConnectRequest connectRequest = new ConnectRequest();
        GenericResponseDTO expectedGenericResponseDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .responseMessage("response message")
                .build();

        connectRequest.requestingUserId = USER1_ID;
        connectRequest.toBeConnectedWithUserId = USER2_ID;
        connectRequest.qrcodePhrase = "ABCDEFGHIJKL";

        when(connectService.connect(anyLong(), anyLong(), anyString())).thenReturn(expectedGenericResponseDTO);

        String template = "{\"booleanMessage\": %b, \"responseMessage\": \"%s\"}";
        String expectedMessage = String.format(template, false, "response message");

        this.mockMvc
                .perform(
                        post("/api/connect")
                                .content(gson.toJson(connectRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedMessage))
                .andReturn();

    }

    @Test
    public void saveCosign() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);

        Long testUserIdIssuing = USER1_ID;
        Long testUserIdReceiving = USER2_ID;
        Long testPhraseId = PHRASE1_ID;

        CosignDTO mockCosignDTO = CosignDTO.builder().build();
        mockCosignDTO.userIdIssuing = testUserIdIssuing;
        mockCosignDTO.userIdReceiving = testUserIdReceiving;
        mockCosignDTO.phraseId = testPhraseId;

        CosignRequest cosignRequest = new CosignRequest();
        cosignRequest.userIdIssuing = testUserIdIssuing;
        cosignRequest.userIdReceiving = testUserIdReceiving;
        cosignRequest.phraseId = testPhraseId;

        when(cosignService.cosign(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(mockCosignDTO));

        String template = "{\"userIdIssuing\": %d},\"userIdReceiving\": %d,\"phraseId\": %d}";
        String expectedMessage = String.format(template, USER1_ID, USER2_ID, PHRASE1_ID);

        this.mockMvc
                .perform(
                        post("/api/connect/cosign")
                                .content(gson.toJson(cosignRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedMessage));

    }

    @Test
    public void saveCosignSadPath() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);

        Long testUserIdIssuing = USER1_ID;
        Long testUserIdReceiving = USER2_ID;
        Long testPhraseId = PHRASE1_ID;

        CosignRequest cosignRequest = new CosignRequest();
        cosignRequest.userIdIssuing = testUserIdIssuing;
        cosignRequest.userIdReceiving = testUserIdReceiving;
        cosignRequest.phraseId = testPhraseId;

        GenericResponseDTO expectedGenericResponseDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .responseMessage("response message")
                .build();

        when(cosignService.cosign(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(expectedGenericResponseDTO));

        String template = "{\"booleanMessage\": %b, \"responseMessage\": \"%s\"}";
        String expectedMessage = String.format(template, false, "response message");

        this.mockMvc
                .perform(
                        post("/api/connect/cosign")
                                .content(gson.toJson(cosignRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedMessage));
    }

    @Test
    public void deleteCosignSadPath() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);

        CosignRequest cosignRequest = new CosignRequest();
        cosignRequest.userIdIssuing = USER1_ID;
        cosignRequest.userIdReceiving = USER2_ID;
        cosignRequest.phraseId = PHRASE1_ID;

        GenericResponseDTO expectedDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .responseMessage("response message")
                .build();

        when(cosignService.deleteCosign(anyLong(),anyLong(),anyLong())).thenReturn(expectedDTO);

        String template = "{\"booleanMessage\": %b, \"responseMessage\": \"%s\"}";
        String expectedMessage = String.format(template, false, "response message");

        this.mockMvc
                .perform(
                        delete("/api/connect/cosign")
                                .content(gson.toJson(cosignRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedMessage));
    }

    @Test
    public void deleteCosignHappyPath() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);

        CosignRequest cosignRequest = new CosignRequest();
        cosignRequest.userIdIssuing = USER1_ID;
        cosignRequest.userIdReceiving = USER2_ID;
        cosignRequest.phraseId = PHRASE1_ID;

        GenericResponseDTO expectedDTO = GenericResponseDTO.builder()
                .booleanMessage(true)
                .build();

        when(cosignService.deleteCosign(anyLong(),anyLong(),anyLong())).thenReturn(expectedDTO);

        String template = "{\"booleanMessage\": %b}";
        String expectedMessage = String.format(template, true);

        this.mockMvc
                .perform(
                        delete("/api/connect/cosign")
                                .content(gson.toJson(cosignRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedMessage));

    }
    
    @Test
    public void testGetConnectionsHappyPath() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);
        Long toBeConnectedWithUserId = USER1_ID;
        User requestingUser = UserTestConstants.getUser2();

        ConnectOutgoingMessageDTO returnDTO = ConnectOutgoingMessageDTO
                .builder()
                .connectionError(null)
                .connectionSuccess(true)
                .message("")
                .to(UsernameDTO.builder()
                        .userId(requestingUser.getId())
                        .username(requestingUser.getName())
                        .build())
                .build();

        List<ConnectOutgoingMessageDTO> expectedReturnDtoList = new ArrayList<>();
        expectedReturnDtoList.add(returnDTO);

        when(connectService.getAllConnectionsForAUser(anyLong())).thenReturn(expectedReturnDtoList);

        MvcResult result =
                this.mockMvc
                        .perform(
                                get("/api/connect/{userId}/all", toBeConnectedWithUserId)
                                        .header("Authorization", "Bearer " + auth)
                                        .characterEncoding("utf-8"))
                        .andExpect(status().isOk())
                        .andReturn();

        Type connectOutgoingMessageListDTOType = new TypeToken<List<ConnectOutgoingMessageDTO>>(){}.getType();

        List<ConnectOutgoingMessageDTO> actualConnectOutingMessages =
                gson.fromJson(result.getResponse().getContentAsString(), connectOutgoingMessageListDTOType);

        assertThat(actualConnectOutingMessages).usingRecursiveComparison().isEqualTo(expectedReturnDtoList);
    }

    @Test
    public void testGetConnectionsSadPath() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);

        Long userId = USER1_ID;

        when(connectService.getAllConnectionsForAUser(anyLong())).thenReturn(null);

        MvcResult result =
                this.mockMvc
                        .perform(
                                get("/api/connect/{userId}/all", userId)
                                        .header("Authorization", "Bearer " + auth)
                                        .characterEncoding("utf-8"))
                        .andExpect(status().isBadRequest())
                        .andReturn();

    }

    @Test
    public void removeConnectionHappyPath() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);

        ConnectionRemovalRequest connectionRemovalRequest = new ConnectionRemovalRequest();
        connectionRemovalRequest.requestingUserId = USER1_ID;
        connectionRemovalRequest.connectedWithUserId = USER2_ID;

        GenericResponseDTO expectedDTO = GenericResponseDTO.builder()
                .booleanMessage(true)
                .build();

        when(connectService.removeConnection(anyLong(), anyLong())).thenReturn(expectedDTO);
        ArgumentCaptor<Long> requestingUserIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> connectedWithIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        String template = "{\"booleanMessage\": %b}";
        String expectedMessage = String.format(template, true);

        this.mockMvc
                .perform(
                        delete("/api/connect")
                                .content(gson.toJson(connectionRemovalRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedMessage))
                .andReturn();

        verify(connectService, times(1)).removeConnection(requestingUserIdArgumentCaptor.capture(), connectedWithIdArgumentCaptor.capture());
        assertThat(requestingUserIdArgumentCaptor.getValue()).isEqualTo(connectionRemovalRequest.requestingUserId);
        assertThat(connectedWithIdArgumentCaptor.getValue()).isEqualTo(connectionRemovalRequest.connectedWithUserId);

    }

    @Test
    public void removeConnectionWhenRemovalUnsuccessful() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);

        ConnectionRemovalRequest connectionRemovalRequest = new ConnectionRemovalRequest();
        connectionRemovalRequest.requestingUserId = USER1_ID;
        connectionRemovalRequest.connectedWithUserId = USER2_ID;

        GenericResponseDTO expectedDTO = GenericResponseDTO.builder()
                .booleanMessage(false)
                .responseMessage("message")
                .build();

        when(connectService.removeConnection(anyLong(), anyLong())).thenReturn(expectedDTO);
        ArgumentCaptor<Long> requestingUserIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> connectedWithIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        String template = "{\"booleanMessage\": %b, \"responseMessage\": \"%s\"}";
        String expectedMessage = String.format(template, false, "message");

        this.mockMvc
                .perform(
                        delete("/api/connect")
                                .content(gson.toJson(connectionRemovalRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedMessage))
                .andReturn();

        verify(connectService, times(1)).removeConnection(requestingUserIdArgumentCaptor.capture(), connectedWithIdArgumentCaptor.capture());
        assertThat(requestingUserIdArgumentCaptor.getValue()).isEqualTo(connectionRemovalRequest.requestingUserId);
        assertThat(connectedWithIdArgumentCaptor.getValue()).isEqualTo(connectionRemovalRequest.connectedWithUserId);

    }


    @Test
    public void testGetCosignersForUserAttribute() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);

        // test data
        User testUserIssuing = UserTestConstants.getUser1();
        Long testUserIdReceiving = USER2_ID;
        Long testPhraseId = PHRASE1_ID;

        // mock return data
        UsernameDTO mockUsernameDTO = UsernameDTO.builder()
                .userId(testUserIssuing.getId())
                .username(testUserIssuing.getName())
                .build();

        List<UsernameDTO> mockUsernameDTOList = new ArrayList<>();
        mockUsernameDTOList.add(mockUsernameDTO);

        // mock returns
        when(cosignService.getCosignersForUserAttribute(anyLong(),anyLong())).thenReturn(mockUsernameDTOList);

        // expected result
        String template = "[{\"userId\": %d,\"username\":\"%s\"}]";
        String expectedMessage = String.format(template,USER1_ID, USER1_NAME);

        // test
        this.mockMvc
                .perform(
                        get("/api/connect/cosign/{userIdReceiving}/{phraseId}",testUserIdReceiving,testPhraseId)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedMessage));
    }

    @Test
    public void testGetAllCosignsForUser() throws Exception {
        when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);

        // test data
        User testUserIssuing1 = UserTestConstants.getUser1();
        User testUserIssuing2 = UserTestConstants.getUser2();
        User testUserIssuing3 = UserTestConstants.getUser3();
        Long testPhraseId1 = PHRASE1_ID;
        Long testPhraseId2 = PHRASE2_ID;
        Long testPhraseId3 = PHRASE3_ID;
        Long testUserIdReceiving = 4L;

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


        List<CosignsForUserDTO> mockCosignsForUserDTOList = new ArrayList<>();
        mockCosignsForUserDTOList.add(mockCosignsForUserDTO1);
        mockCosignsForUserDTOList.add(mockCosignsForUserDTO2);
        mockCosignsForUserDTOList.add(mockCosignsForUserDTO3);

        // mock returns
        when(cosignService.getAllCosignsForUser(anyLong())).thenReturn(mockCosignsForUserDTOList);

        // expected result
        String template =
                "[" +
                        "{\"phraseId\":%d,\"listOfCosigners\":" +
                        "[" +
                        "{\"userId\":%d,\"username\":\"%s\"}," +
                        "{\"userId\":%d,\"username\":\"%s\"}," +
                        "{\"userId\":%d,\"username\":\"%s\"}" +
                        "]" +
                        "}," +
                        "{\"phraseId\":%d,\"listOfCosigners\":" +
                        "[" +
                        "{\"userId\":%d,\"username\":\"%s\"}," +
                        "{\"userId\":%d,\"username\":\"%s\"}," +
                        "{\"userId\":%d,\"username\":\"%s\"}" +
                        "]" +
                        "}," +
                        "{\"phraseId\":%d,\"listOfCosigners\":" +
                        "[" +
                        "{\"userId\":%d,\"username\":\"%s\"}," +
                        "{\"userId\":%d,\"username\":\"%s\"}," +
                        "{\"userId\":%d,\"username\":\"%s\"}" +
                        "]" +
                        "}]";
        String expectedMessage = String.format(
                template,
                PHRASE1_ID, USER1_ID, USER1_NAME, USER2_ID, USER2_NAME, USER3_ID, USER3_NAME,
                PHRASE2_ID, USER1_ID, USER1_NAME, USER2_ID, USER2_NAME, USER3_ID, USER3_NAME,
                PHRASE3_ID, USER1_ID, USER1_NAME, USER2_ID, USER2_NAME, USER3_ID, USER3_NAME
                );

        // test
        this.mockMvc
                .perform(
                        get("/api/connect/cosign/{userIdReceiving}/all",testUserIdReceiving)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedMessage));
    }
}

