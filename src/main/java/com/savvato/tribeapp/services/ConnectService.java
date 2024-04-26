package com.savvato.tribeapp.services;

import com.savvato.tribeapp.controllers.dto.ConnectRequest;
import com.savvato.tribeapp.controllers.dto.ConnectionRemovalRequest;
import com.savvato.tribeapp.dto.ConnectOutgoingMessageDTO;
import com.savvato.tribeapp.dto.GenericResponseDTO;

import java.util.List;
import java.util.Optional;

public interface ConnectService {

    List<ConnectOutgoingMessageDTO> getAllConnectionsForAUser(Long userId);

    Optional<String> getQRCodeString(long userId);

    Optional<String> storeQRCodeString(long userId);

    Boolean validateQRCode(String qrcodePhrase, Long toBeConnectedWithUserId);

    boolean saveConnectionDetails(Long requestingUserId, Long toBeConnectedWithUserId);

    GenericResponseDTO connect(Long requestingUserId, Long toBeConnectedWithUserId, String qrcodePhrase);

    GenericResponseDTO removeConnection(Long requestingUserId, Long connectedWithUserId);

    Optional<GenericResponseDTO> validateConnection(Long requestingUserId, Long toBeConnectedWithUserId);
}
