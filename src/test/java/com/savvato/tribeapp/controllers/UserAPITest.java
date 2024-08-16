package com.savvato.tribeapp.controllers;

import com.savvato.tribeapp.constants.UserTestConstants;
import com.savvato.tribeapp.controllers.dto.ChangePasswordRequest;
import com.savvato.tribeapp.controllers.dto.UserRequest;
import com.savvato.tribeapp.dto.UserDTO;
import com.savvato.tribeapp.dto.UserRoleDTO;
import com.savvato.tribeapp.entities.User;
import com.savvato.tribeapp.repositories.UserRepository;
import com.savvato.tribeapp.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAPITest implements UserTestConstants {

    @InjectMocks
    private UserAPIController userAPIController;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Captor
    ArgumentCaptor<UserRequest> userRequestCaptor;

    @Captor
    ArgumentCaptor<String> preferredContactMethodCaptor;

    @Captor
    ArgumentCaptor<String> availabilityQueryCaptor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createUserHappyPath() {
        User user = UserTestConstants.getUser3();

        UserRequest userRequest = new UserRequest();
        userRequest.id = user.getId();
        userRequest.name = user.getName();
        userRequest.phone = user.getPhone();
        userRequest.email = user.getEmail();
        userRequest.password = user.getPassword();
        Optional<User> userOpt = Optional.of(user);

        when(userService.createNewUser(any(UserRequest.class), anyString())).thenReturn(userOpt);

        ResponseEntity<User> response = userAPIController.createUser(userRequest);

        verify(userService, times(1))
                .createNewUser(userRequestCaptor.capture(), preferredContactMethodCaptor.capture());
        assertThat(userRequestCaptor.getValue()).usingRecursiveComparison().isEqualTo(userRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    public void createUserWhenErrorThrown() {
        UserRequest userRequest = new UserRequest();
        userRequest.id = null;
        userRequest.name = null;
        userRequest.phone = UserTestConstants.USER2_PHONE;
        userRequest.email = UserTestConstants.USER2_EMAIL;
        userRequest.password = null;
        String errorMessage = "Missing critical UserRequest values.";

        when(userService.createNewUser(any(UserRequest.class), anyString()))
                .thenThrow(new IllegalArgumentException(errorMessage));

        ResponseEntity<String> response = userAPIController.createUser(userRequest);

        verify(userService, times(1))
                .createNewUser(userRequestCaptor.capture(), preferredContactMethodCaptor.capture());
        assertThat(userRequestCaptor.getValue()).usingRecursiveComparison().isEqualTo(userRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorMessage);
    }

    @Test
    public void isUsernameAvailable() {
        String username = USER1_NAME;

        when(userRepository.findByName(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new User()));

        // when userRepository returns empty Optional
        boolean isAvailable = userAPIController.isUsernameAvailable(username);
        assertThat(isAvailable).isTrue();

        // when userRepository returns a User
        isAvailable = userAPIController.isUsernameAvailable(username);
        assertThat(isAvailable).isFalse();

        verify(userRepository, times(2)).findByName(availabilityQueryCaptor.capture());
        assertEquals(availabilityQueryCaptor.getAllValues().get(0), username);
        assertEquals(availabilityQueryCaptor.getAllValues().get(1), username);
    }

    @Test
    public void isEmailAddressAvailable() {
        String email = USER2_EMAIL;

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new User()));

        // when userRepository returns empty Optional
        boolean isAvailable = userAPIController.isEmailAddressAvailable(email);
        assertThat(isAvailable).isTrue();

        // when userRepository returns a User
        isAvailable = userAPIController.isEmailAddressAvailable(email);
        assertThat(isAvailable).isFalse();

        verify(userRepository, times(2)).findByEmail(availabilityQueryCaptor.capture());
        assertEquals(availabilityQueryCaptor.getAllValues().get(0), email);
        assertEquals(availabilityQueryCaptor.getAllValues().get(1), email);
    }

    @Test
    public void isPhoneNumberAvailable() {
        String phone = USER2_PHONE;

        when(userRepository.findByPhone(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new ArrayList<>()))
                .thenReturn(Optional.of(List.of(new User())));

        // when userRepository returns an empty Optional
        boolean isAvailable = userAPIController.isPhoneNumberAvailable(phone);
        assertThat(isAvailable).isTrue();

        // when userRepository returns an empty list
        isAvailable = userAPIController.isPhoneNumberAvailable(phone);
        assertThat(isAvailable).isTrue();

        // when userRepository returns a list containing a User
        isAvailable = userAPIController.isPhoneNumberAvailable(phone);
        assertThat(isAvailable).isFalse();
    }

    @Test
    public void isUserInformationUniqueWhenUsernameTaken() {
        String email = USER2_EMAIL;
        String phone = USER2_PHONE;
        String username = USER2_NAME;
        String password = USER2_PASSWORD;

        when(userRepository.findByName(anyString()))
                .thenReturn(Optional.of(new User(username, password, phone, email)));

        String expectedMessage = "{\"response\": \"username\"}";

        String response = userAPIController.isUserInformationUnique(username, phone, email);
        assertThat(response).isEqualTo(expectedMessage);
    }

    @Test
    public void isUserInformationUniqueWhenEmailTaken() {
        String email = USER2_EMAIL;
        String phone = USER2_PHONE;
        String username = USER2_NAME;
        String password = USER2_PASSWORD;

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new User(username, password, phone, email)));

        String expectedMessage = "{\"response\": \"email\"}";

        String response = userAPIController.isUserInformationUnique(username, phone, email);
        assertThat(response).isEqualTo(expectedMessage);
    }

    @Test
    public void isUserInformationUniqueWhenPhoneTaken() {
        String email = USER2_EMAIL;
        String phone = USER2_PHONE;
        String username = USER2_NAME;
        String password = USER2_PASSWORD;

        when(userRepository.findByPhone(anyString())).thenReturn(Optional.of(List.of(new User(username, password, phone, email))));

        String expectedMessage = "{\"response\": \"phone\"}";

        String response = userAPIController.isUserInformationUnique(username, phone, email);
        assertThat(response).isEqualTo(expectedMessage);
    }

    @Test
    public void isUserInformationUniqueHappyPath() {
        String email = USER2_EMAIL;
        String phone = USER2_PHONE;
        String username = USER2_NAME;

        String expectedMessage = "{\"response\": true}";

        String response = userAPIController.isUserInformationUnique(username, phone, email);
        assertThat(response).isEqualTo(expectedMessage);
    }

    @Test
    public void changePassword() {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.phoneNumber = USER2_PHONE;
        changePasswordRequest.pw = "admin";
        changePasswordRequest.smsChallengeCode = "ABCDEF";

        UserDTO expectedUserDTO =
                UserDTO.builder()
                        .id(USER2_ID)
                        .name(USER2_NAME)
                        .password("not an admin")
                        .phone(changePasswordRequest.phoneNumber)
                        .email(USER2_EMAIL)
                        .enabled(1)
                        .roles(Set.of(UserRoleDTO.builder().id(1L).name("ROLE_ACCOUNTHOLDER").build()))
                        .build();

        when(userService.changePassword(anyString(), anyString(), anyString())).thenReturn(expectedUserDTO);

        UserDTO result = userAPIController.changePassword(changePasswordRequest);

        assertThat(result).usingRecursiveComparison().isEqualTo(expectedUserDTO);
    }
}
