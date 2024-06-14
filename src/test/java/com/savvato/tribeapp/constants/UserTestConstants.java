package com.savvato.tribeapp.constants;

import com.savvato.tribeapp.controllers.dto.UserRequest;
import com.savvato.tribeapp.dto.UsernameDTO;
import com.savvato.tribeapp.entities.User;
import com.savvato.tribeapp.entities.UserRole;

import java.util.HashSet;
import java.util.Set;

public interface UserTestConstants {
    long USER1_ID = 1;
    long USER2_ID = 732;
    long USER3_ID = 3;

    String USER1_EMAIL = "user1@email.com";
    String USER2_EMAIL = "user2@email.com";
    String USER3_EMAIL = "user3@email.com";

    String USER1_PHONE = "0035551212"; // starts with 0 to indicate to the code that this is a test
    String USER2_PHONE = "0035551213"; // starts with 0 to indicate to the code that this is a test
    String USER3_PHONE = "0035551214"; // starts with 0 to indicate to the code that this is a test

    String USER1_PASSWORD = "password1";
    String USER2_PASSWORD = "password2";
    String USER3_PASSWORD = "password3";

    int USER_IS_ENABLED = 1;

    String USER1_PREFERRED_CONTACT_METHOD = "email";
    String USER2_PREFERRED_CONTACT_METHOD = "phone";
    String USER3_PREFERRED_CONTACT_METHOD = "phone";

    String USER1_NAME = "Fake A. Admin";
    String USER2_NAME = "Fake R. User"; // the R stand for Regular
    String USER3_NAME = "Fake F. AdminUser"; //the F stands for Full Admin and Account holder

    static Set<UserRole> getUserRoles_AccountHolder() {
        Set<UserRole> rtn = new HashSet<>();
        rtn.add(UserRole.ROLE_ACCOUNTHOLDER);
        return rtn;
    }

    static Set<UserRole> getUserRoles_Admin() {
        Set<UserRole> rtn = new HashSet<>();

        rtn.add(UserRole.ROLE_ADMIN);
        rtn.add(UserRole.ROLE_ACCOUNTHOLDER);

        return rtn;
    }

    static Set<UserRole> getUserRoles_Admin_AccountHolder() {
        Set<UserRole> rtn = new HashSet<>();

        rtn.add(UserRole.ROLE_ADMIN);
        rtn.add(UserRole.ROLE_PHRASEREVIEWER);
        rtn.add(UserRole.ROLE_ACCOUNTHOLDER);

        return rtn;
    }

    static User getUser1() {
        User rtn = new User();

        rtn.setId(USER1_ID);
        rtn.setName(USER1_NAME);
        rtn.setEmail(USER1_EMAIL);
        rtn.setPhone(USER1_PHONE);
        rtn.setPassword(USER1_PASSWORD);
        rtn.setEnabled(USER_IS_ENABLED);
        rtn.setCreated();
        rtn.setLastUpdated();
        rtn.setRoles(getUserRoles_Admin());

        return rtn;
    }

    static User getUser2() {
        User rtn = new User();

        rtn.setId(USER2_ID);
        rtn.setName(USER2_NAME);
        rtn.setEmail(USER2_EMAIL);
        rtn.setPhone(USER2_PHONE);
        rtn.setPassword(USER2_PASSWORD);
        rtn.setEnabled(USER_IS_ENABLED);
        rtn.setCreated();
        rtn.setLastUpdated();
        rtn.setRoles(getUserRoles_Admin());

        return rtn;
    }

    static User getUser3() {
        User rtn = new User();

        rtn.setId(USER3_ID);
        rtn.setName(USER3_NAME);
        rtn.setEmail(USER3_EMAIL);
        rtn.setPhone(USER3_PHONE);
        rtn.setPassword(USER3_PASSWORD);
        rtn.setEnabled(USER_IS_ENABLED);
        rtn.setCreated();
        rtn.setLastUpdated();
        rtn.setRoles(getUserRoles_Admin_AccountHolder());

        return rtn;
    }

    static UsernameDTO getUsernameDTOForUserID(Long userId) {
        if (userId == USER1_ID) {
            return UsernameDTO.builder()
                    .userId(USER1_ID)
                    .username(USER1_NAME)
                    .build();
        } else if (userId == USER2_ID) {
            return UsernameDTO.builder()
                    .userId(USER2_ID)
                    .username(USER2_NAME)
                    .build();
        } else if (userId == USER3_ID) {
            return UsernameDTO.builder()
                    .userId(USER3_ID)
                    .username(USER3_NAME)
                    .build();
        } else {
            return null;
        }
    }

    static UserRequest getUserRequestFor(User user) {
        UserRequest rtn = new UserRequest();

        rtn.id = user.getId();
        rtn.name = user.getName();
        rtn.email = user.getEmail();
        rtn.phone = user.getPhone();
        rtn.password = user.getPassword();

        return rtn;
    }
}
