package com.smit.orbisIn.connectionsService.service;

import com.smit.orbisIn.connectionsService.context.ContextHolder;
import com.smit.orbisIn.connectionsService.dto.ConnectionRequestDto;
import com.smit.orbisIn.connectionsService.entity.ConnectionRequest;
import com.smit.orbisIn.connectionsService.entity.Person;
import com.smit.orbisIn.connectionsService.repository.ConnectionRequestRepository;
import com.smit.orbisIn.connectionsService.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectionsService {

    private final PersonRepository personRepository;
    private final ConnectionRequestRepository requestRepository;
    private final com.smit.orbisIn.connectionsService.producer.ConnectionEventProducer connectionEventProducer;

    private Long currentUserId() {
        Long userId = ContextHolder.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid user context");
        }
        return userId;
    }

    public List<Person> getFirstDegreeConnectionsOfUser(Long userId) {
        log.info("Getting first degree connections of user with ID: {}", userId);

        return personRepository.getFirstDegreeConnections(userId);
    }

    public void publishConnectionEvent(Long senderId, Long receiverId, String status) {
        log.info("Publishing connection event senderId: {} receiverId: {} status: {}", senderId, receiverId, status);
        connectionEventProducer.sendConnectionEvent(
                new com.smit.orbisIn.connectionsService.event.ConnectionEvent(senderId, receiverId, status));
    }

    public ConnectionRequestDto sendRequest(Long receiverId) {
        Long senderId = currentUserId();
        if (senderId.equals(receiverId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot connect with yourself");
        }
        personRepository.findByUserId(receiverId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + receiverId));
        if (personRepository.areConnected(senderId, receiverId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already connected");
        }
        if (requestRepository.findBySenderUserIdAndReceiverUserIdAndStatus(
                senderId, receiverId, ConnectionRequest.PENDING).isPresent()
                || requestRepository.findBySenderUserIdAndReceiverUserIdAndStatus(
                receiverId, senderId, ConnectionRequest.PENDING).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A pending request already exists");
        }
        log.info("User {} sending connection request to {}", senderId, receiverId);
        ConnectionRequest request = new ConnectionRequest();
        request.setSenderUserId(senderId);
        request.setReceiverUserId(receiverId);
        request.setStatus(ConnectionRequest.PENDING);
        request = requestRepository.save(request);
        try {
            publishConnectionEvent(senderId, receiverId, "REQUESTED");
        } catch (Exception e) {
            log.error("Request saved but failed to publish ConnectionEvent", e);
        }
        return toDto(request);
    }

    public List<ConnectionRequestDto> receivedRequests() {
        Long userId = currentUserId();
        return requestRepository.findByReceiverUserIdAndStatus(userId, ConnectionRequest.PENDING)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ConnectionRequestDto> sentRequests() {
        Long userId = currentUserId();
        return requestRepository.findBySenderUserIdAndStatus(userId, ConnectionRequest.PENDING)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public ConnectionRequestDto acceptRequest(Long requestId) {
        Long userId = currentUserId();
        ConnectionRequest request = requestRepository.findById(requestId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found with ID: " + requestId));
        if (!ConnectionRequest.PENDING.equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request is no longer pending");
        }
        if (!request.getReceiverUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the receiver can accept this request");
        }
        request.setStatus(ConnectionRequest.ACCEPTED);
        request = requestRepository.save(request);
        personRepository.createConnection(request.getSenderUserId(), request.getReceiverUserId());
        log.info("User {} accepted connection request {}", userId, requestId);
        try {
            publishConnectionEvent(userId, request.getSenderUserId(), "ACCEPTED");
        } catch (Exception e) {
            log.error("Request accepted but failed to publish ConnectionEvent", e);
        }
        return toDto(request);
    }

    public ConnectionRequestDto rejectRequest(Long requestId) {
        Long userId = currentUserId();
        ConnectionRequest request = requestRepository.findById(requestId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found with ID: " + requestId));
        if (!ConnectionRequest.PENDING.equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request is no longer pending");
        }
        if (!request.getReceiverUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the receiver can reject this request");
        }
        request.setStatus(ConnectionRequest.REJECTED);
        request = requestRepository.save(request);
        log.info("User {} rejected connection request {}", userId, requestId);
        return toDto(request);
    }

    public List<Person> discover(String q) {
        Long userId = currentUserId();
        List<Long> excluded = new ArrayList<>();
        excluded.add(userId);
        excluded.addAll(personRepository.getFirstDegreeConnections(userId)
                .stream().map(Person::getUserId).collect(Collectors.toList()));
        excluded.addAll(requestRepository.findByReceiverUserIdAndStatus(userId, ConnectionRequest.PENDING)
                .stream().map(ConnectionRequest::getSenderUserId).collect(Collectors.toList()));
        excluded.addAll(requestRepository.findBySenderUserIdAndStatus(userId, ConnectionRequest.PENDING)
                .stream().map(ConnectionRequest::getReceiverUserId).collect(Collectors.toList()));
        return personRepository.discover(excluded, q == null ? "" : q.trim());
    }

    private ConnectionRequestDto toDto(ConnectionRequest request) {
        ConnectionRequestDto dto = new ConnectionRequestDto();
        dto.setId(request.getId());
        dto.setSenderUserId(request.getSenderUserId());
        dto.setReceiverUserId(request.getReceiverUserId());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setSenderName(personRepository.findByUserId(request.getSenderUserId())
                .map(Person::getName).orElse("User " + request.getSenderUserId()));
        dto.setReceiverName(personRepository.findByUserId(request.getReceiverUserId())
                .map(Person::getName).orElse("User " + request.getReceiverUserId()));
        return dto;
    }
}
