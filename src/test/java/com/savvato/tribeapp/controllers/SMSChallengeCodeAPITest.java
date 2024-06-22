package com.savvato.tribeapp.controllers;

import com.google.gson.Gson;
import com.savvato.tribeapp.config.SecurityConfig;
import com.savvato.tribeapp.config.principal.UserPrincipal;
//import com.savvato.tribeapp.constants.Constants;
import com.savvato.tribeapp.constants.UserTestConstants;
import com.savvato.tribeapp.controllers.dto.SMSChallengeRequest;
import com.savvato.tribeapp.entities.User;
import com.savvato.tribeapp.entities.UserRole;
import com.savvato.tribeapp.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SMSChallengeCodeAPIController.class)
@Import(SecurityConfig.class)
public class SMSChallengeCodeAPITest implements UserTestConstants {
    
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
    private SMSChallengeCodeService smsccs;

    @Captor
    private ArgumentCaptor<String> phoneNumberCaptor;

    @Captor
    private ArgumentCaptor<String> codeCaptor;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                        .apply(springSecurity())
                        .build();

        user = UserTestConstants.getUser3();

    }

    @ParameterizedTest
    @ValueSource(strings = {UserTestConstants.USER2_PHONE, "0123456789"})
    public void sendSMSChallengeCode(String phoneNumber) throws Exception {
        SMSChallengeRequest smsChallengeRequest = new SMSChallengeRequest();
        smsChallengeRequest.code = "ABCDEF";
        smsChallengeRequest.phoneNumber = phoneNumber;
        String expectedPhoneNumber = phoneNumber.startsWith("0") ? phoneNumber : "1" + phoneNumber;

        when(smsccs.sendSMSChallengeCodeToPhoneNumber(anyString())).thenReturn(smsChallengeRequest.code);
        this.mockMvc
                .perform(
                        post("/api/public/sendSMSChallengeCodeToPhoneNumber")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(smsChallengeRequest))
                                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().string(smsChallengeRequest.code));

        verify(smsccs, times(1)).sendSMSChallengeCodeToPhoneNumber(phoneNumberCaptor.capture());
        assertEquals(phoneNumberCaptor.getValue(), expectedPhoneNumber);
    }

    @Test
    public void clearSMSChallengeCode() throws Exception {

        Mockito.when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);
        String phoneNumber = UserTestConstants.USER2_PHONE;
        String expectedPhoneNumber = "1" + phoneNumber;

        this.mockMvc
                .perform(
                        post("/api/public/clearSMSChallengeCodeToPhoneNumber")
                                .param("phoneNumber", phoneNumber)
                                .header("Authorization", "Bearer " + auth)
                                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        verify(smsccs, times(1)).clearSMSChallengeCodeToPhoneNumber(phoneNumberCaptor.capture());
        assertEquals(phoneNumberCaptor.getValue(), expectedPhoneNumber);
    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    public void isAValidSMSChallengeCodeWhenRequestInvalid(String phoneNumber, String code) throws Exception {

        Mockito.when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString()))
                .thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);
        String errorMessage = "Cannot check for valid SMS challenge code with null phoneNumber or challenge code.";
        SMSChallengeRequest smsChallengeRequest = new SMSChallengeRequest();
        smsChallengeRequest.phoneNumber = phoneNumber;
        smsChallengeRequest.code = code;
        // TODO: Figure out how to catch exception thrown in controller
        /**
         assertThrows(IllegalArgumentException.class, () -> {
         this.mockMvc
         .perform(
         post("/api/public/isAValidSMSChallengeCode")
         .contentType(MediaType.APPLICATION_JSON)
         .content(gson.toJson(smsChallengeRequest))
         .header("Authorization", "Bearer " + auth)
         .characterEncoding("utf-8"));
         });*/


    }

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of(null, "ABCDEF"),
                Arguments.of("null", "ABCDEF"),
                Arguments.of(UserTestConstants.USER2_PHONE, null),
                Arguments.of(null, "null")
        );
    }
}
