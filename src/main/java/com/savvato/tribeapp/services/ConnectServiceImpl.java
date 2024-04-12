package com.savvato.tribeapp.services;

import com.savvato.tribeapp.controllers.dto.ConnectRequest;
import com.savvato.tribeapp.controllers.dto.ConnectionRemovalRequest;
import com.savvato.tribeapp.dto.ConnectOutgoingMessageDTO;
import com.savvato.tribeapp.dto.GenericResponseDTO;
import com.savvato.tribeapp.dto.UsernameDTO;
import com.savvato.tribeapp.entities.Connection;
import com.savvato.tribeapp.repositories.ConnectionsRepository;
import com.savvato.tribeapp.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ConnectServiceImpl implements ConnectService {

    @Autowired
    CacheService cache;

    @Autowired
    ConnectionsRepository connectionsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    GenericResponseService genericResponseService;

    private final int QRCODE_STRING_LENGTH = 12;

    public Optional<String> getQRCodeString(long userId) {
        String userIdToCacheKey = String.valueOf(userId);
        String getCode = cache.get("ConnectQRCodeString", userIdToCacheKey);
        Optional<String> opt = Optional.ofNullable(getCode);
        return opt;
    }

    public Optional<String> storeQRCodeString(long userId) {
        String generatedQRCodeString = generateRandomString(QRCODE_STRING_LENGTH);
        String userIdToCacheKey = String.valueOf(userId);
        cache.put("ConnectQRCodeString", userIdToCacheKey, generatedQRCodeString);
        log.debug("User ID: " + userId + " ConnectQRCodeString: " + generatedQRCodeString);
        return Optional.of(generatedQRCodeString);
    }

    private String generateRandomString(int length) {
        Random random = new Random();
        char[] digits = new char[length];
        digits[0] = (char) (random.nextInt(9) + '1');
        for (int i = 1; i < length; i++) {
            digits[i] = (char) (random.nextInt(10) + '0');
        }
        return new String(digits);
    }

    public boolean saveConnectionDetails(Long requestingUserId, Long toBeConnectedWithUserId) {

        try {
            connectionsRepository.save(new Connection(requestingUserId, toBeConnectedWithUserId));
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public Boolean validateQRCode(String qrcodePhrase, Long toBeConnectedWithUserId) {
        return qrcodePhrase.equals(getQRCodeString(toBeConnectedWithUserId).orElse("")) && StringUtils.isNotBlank(qrcodePhrase);
    }

    @Override
    public GenericResponseDTO connect(ConnectRequest connectRequest) {

        GenericResponseDTO genericResponseDTO = GenericResponseDTO.builder().build();

        if (!validateQRCode(connectRequest.qrcodePhrase, connectRequest.toBeConnectedWithUserId)) {
            genericResponseDTO.booleanMessage = false;
            genericResponseDTO.responseMessage = "Unable to validate QR code.";
            return genericResponseDTO;
        }

        Optional<GenericResponseDTO> optValidateConnection = validateConnection(connectRequest.requestingUserId,connectRequest.toBeConnectedWithUserId);

        if (optValidateConnection.isPresent()) {
            return optValidateConnection.get();
        }

        genericResponseDTO.booleanMessage = saveConnectionDetails(connectRequest.requestingUserId, connectRequest.toBeConnectedWithUserId);

        return genericResponseDTO;
    }

    @Override
    public List<ConnectOutgoingMessageDTO> getAllConnectionsForAUser(Long userId) {
        List<Connection> connections = connectionsRepository.findAllByToBeConnectedWithUserId(userId);
        List<ConnectOutgoingMessageDTO> outgoingMessages = new ArrayList<>();
        for (Connection connection : connections) {
            ConnectOutgoingMessageDTO outgoingMessage = ConnectOutgoingMessageDTO.builder()
                    .connectionSuccess(true)
                    .to(UsernameDTO.builder()
                            .userId(connection.getRequestingUserId())
                            .username(userRepository.findById(connection.getRequestingUserId()).get().getName())
                            .build())
                    .message("")
                    .build();
            outgoingMessages.add(outgoingMessage);
        }
        return outgoingMessages;
    }

    public boolean removeConnection(ConnectionRemovalRequest connectionRemovalRequest) {
        if (Objects.equals(connectionRemovalRequest.requestingUserId, connectionRemovalRequest.connectedWithUserId)) {
            return false;
        }
        try {
            connectionsRepository.removeConnection(connectionRemovalRequest.requestingUserId, connectionRemovalRequest.connectedWithUserId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<GenericResponseDTO> validateConnection(Long requestingUserId, Long toBeConnectedWithUserId) {
        GenericResponseDTO genericResponseDTO = GenericResponseDTO.builder().build();
        Long loggedInUser = userService.getLoggedInUserId();

        if (!loggedInUser.equals(requestingUserId)) {
            genericResponseDTO.booleanMessage = false;
            genericResponseDTO.responseMessage = "The logged in user (" + loggedInUser + ") does not match issuing user (" + requestingUserId + ")";
            return Optional.of(genericResponseDTO);
        }

        if (requestingUserId.equals(toBeConnectedWithUserId)) {
            genericResponseDTO.booleanMessage = false;
            genericResponseDTO.responseMessage = "User " + requestingUserId + " may not connect with themselves";
            return Optional.of(genericResponseDTO);
        }

        Optional<Connection> existingConnectionWithReversedIds = connectionsRepository.findExistingConnectionWithReversedUserIds(requestingUserId, toBeConnectedWithUserId);

        if (existingConnectionWithReversedIds.isPresent()) {
            genericResponseDTO.booleanMessage = false;
            genericResponseDTO.responseMessage = "This connection already exists in reverse between the requesting user " + requestingUserId + " and the to be connected with user " + toBeConnectedWithUserId;
            return Optional.of(genericResponseDTO);
        }

        return Optional.empty();
    }
}
