package com.smit.orbisIn.connectionsService.repository;

import com.smit.orbisIn.connectionsService.entity.ConnectionRequest;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectionRequestRepository extends Neo4jRepository<ConnectionRequest, Long> {

    List<ConnectionRequest> findByReceiverUserIdAndStatus(Long receiverUserId, String status);

    List<ConnectionRequest> findBySenderUserIdAndStatus(Long senderUserId, String status);

    Optional<ConnectionRequest> findBySenderUserIdAndReceiverUserIdAndStatus(
            Long senderUserId, Long receiverUserId, String status);
}
