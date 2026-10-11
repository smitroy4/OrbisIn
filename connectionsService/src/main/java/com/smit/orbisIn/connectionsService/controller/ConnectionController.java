package com.smit.orbisIn.connectionsService.controller;

import com.smit.orbisIn.connectionsService.dto.ConnectionRequestDto;
import com.smit.orbisIn.connectionsService.entity.Person;
import com.smit.orbisIn.connectionsService.service.ConnectionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/core")
public class ConnectionController {

    private final ConnectionsService connectionsService;

    @GetMapping("/{userId}/first-degree")
    public ResponseEntity<List<Person>> getFirstDegreeConnections(@PathVariable Long userId) {
        List<Person> personList = connectionsService.getFirstDegreeConnectionsOfUser(userId);
        return ResponseEntity.ok(personList);
    }

    @GetMapping("/discover")
    public ResponseEntity<List<Person>> discover(@RequestParam(required = false, defaultValue = "") String q) {
        return ResponseEntity.ok(connectionsService.discover(q));
    }

    @PostMapping("/requests/{receiverId}")
    public ResponseEntity<ConnectionRequestDto> sendRequest(@PathVariable Long receiverId) {
        ConnectionRequestDto dto = connectionsService.sendRequest(receiverId);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @GetMapping("/requests/received")
    public ResponseEntity<List<ConnectionRequestDto>> receivedRequests() {
        return ResponseEntity.ok(connectionsService.receivedRequests());
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<ConnectionRequestDto>> sentRequests() {
        return ResponseEntity.ok(connectionsService.sentRequests());
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<ConnectionRequestDto> acceptRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(connectionsService.acceptRequest(requestId));
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<ConnectionRequestDto> rejectRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(connectionsService.rejectRequest(requestId));
    }
}
