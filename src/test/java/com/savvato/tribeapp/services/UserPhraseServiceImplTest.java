package com.savvato.tribeapp.services;

import com.savvato.tribeapp.constants.PhraseTestConstants;
import com.savvato.tribeapp.constants.UserTestConstants;
import com.savvato.tribeapp.repositories.UserPhraseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class UserPhraseServiceImplTest implements UserTestConstants, PhraseTestConstants {
    @TestConfiguration
    static class UserPhraseServiceTestContextConfiguration {
        @Bean
        public UserPhraseService userPhraseService() {
            return new UserPhraseServiceImpl();
        }
    }

    @Autowired
    UserPhraseService userPhraseService;
    @MockBean
    UserPhraseRepository userPhraseRepository;

    @Test
    public void findPhraseIdsByUserId() {
        Long userId = USER1_ID;
        List<Long> phraseIds = new ArrayList<>(List.of(PHRASE1_ID, PHRASE2_ID, PHRASE3_ID));
        Optional<List<Long>> phraseIdsOpt = Optional.of(phraseIds);
        when(userPhraseRepository.findPhraseIdsByUserId(any())).thenReturn(phraseIdsOpt);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        Optional<List<Long>> result = userPhraseService.findPhraseIdsByUserId(userId);
        verify(userPhraseRepository, times(1)).findPhraseIdsByUserId(userIdCaptor.capture());
        assertEquals(userId, userIdCaptor.getValue());
        assertThat(result).usingRecursiveComparison().isEqualTo(phraseIdsOpt);
    }
}
