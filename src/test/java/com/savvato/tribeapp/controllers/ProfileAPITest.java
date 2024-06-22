package com.savvato.tribeapp.controllers;

import com.savvato.tribeapp.config.SecurityConfig;
import com.savvato.tribeapp.config.principal.UserPrincipal;
import com.savvato.tribeapp.constants.UserTestConstants;
import com.savvato.tribeapp.dto.ProfileDTO;
import com.savvato.tribeapp.dto.GenericResponseDTO;
import com.savvato.tribeapp.entities.User;
import com.savvato.tribeapp.entities.UserRole;
import com.savvato.tribeapp.services.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileAPIController.class)
@Import(SecurityConfig.class)
public class ProfileAPITest implements UserTestConstants {

    private UserPrincipal userPrincipal;
    private User user;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsServiceTRIBEAPP userDetailsServiceTRIBEAPP;

    @MockBean
    private GenericResponseService GenericResponseService;

    @MockBean
    private UserPrincipalService userPrincipalService;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private AuthService authService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(this.webApplicationContext)
                .apply(springSecurity())
                .build();

        user = UserTestConstants.getUser3();

    }

    @Test
    public void testProfileHappyPath() throws Exception {

        Mockito.when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString())).thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);

        Mockito.when(profileService.getByUserId(Mockito.anyLong())).thenReturn(
                Optional.of(ProfileDTO.builder()
                        .name(USER1_NAME)
                        .email(USER2_EMAIL)
                        .phone(USER1_PHONE)
                        .build())
        );

        String template = "{\"name\": \"%s\"}";
        String expectedMessage = String.format(template, USER1_NAME);

        this.mockMvc.
                perform(
                    get("/api/profile/1")
                    .header("Authorization", "Bearer " + auth)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(expectedMessage));

              
                
    }
     @Test
    public void testProfileHappyPathUpdate() throws Exception {

        Mockito.when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString())).thenReturn(new UserPrincipal(user));
         String auth = AuthServiceImpl.generateAccessToken(user);

        Mockito.when(profileService.update(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(true);

         when(GenericResponseService.createDTO(
                 anyBoolean()))
                 .thenReturn(GenericResponseDTO.builder()
                         .booleanMessage(true)
                         .build());

         String template = "{\"booleanMessage\": %b}";
         String expectedMessage = String.format(template, true);

        this.mockMvc.
        perform(
            put("/api/profile/1")
            .header("Authorization", "Bearer " + auth)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content("{\"userId\":\"1\",\"name\":\"bob\",\"email\":\"admin@app.com\",\"phone\":\"3035551212\"}")
        )
        
        .andExpect(status().isOk())
        .andExpect(content().json(expectedMessage));

    }

    @Test
    public void testProfileUnHappyPathUpdate() throws Exception {

    Mockito.when(userPrincipalService.getUserPrincipalByEmail(Mockito.anyString())).thenReturn(new UserPrincipal(user));
        String auth = AuthServiceImpl.generateAccessToken(user);

    Mockito.when(profileService.update(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
    .thenReturn(false);

    when(GenericResponseService.createDTO(
            anyBoolean()))
            .thenReturn(GenericResponseDTO.builder()
                    .booleanMessage(false)
                    .build());

    String template = "{\"booleanMessage\": %b}";
    String expectedMessage = String.format(template, false);

    this.mockMvc.
    perform(
        put("/api/profile/1")
        .header("Authorization", "Bearer " + auth)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content("{\"userId\":\"1\",\"name\":\"bob\",\"email\":\"admin\",\"phone\":\"3035551212\"}")
    )
    
    .andExpect(status().isBadRequest())
    .andExpect(content().json(expectedMessage));

    }

}