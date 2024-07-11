package com.rocketseat.planner.trip;

import com.rocketseat.planner.activity.ActivityRequestPayload;
import com.rocketseat.planner.activity.ActivityResponse;
import com.rocketseat.planner.activity.ActivityService;
import com.rocketseat.planner.participant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {
    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private TripRepository repository;

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload) {
        Trip newTrip = new Trip(payload);

        this.repository.save(newTrip);
        this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
        Optional<Trip> trip = repository.findById(id);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("{id}")
    public ResponseEntity<Trip> updateTripe(@PathVariable UUID id, @RequestBody TripRequestPayload payload) {
        Optional<Trip> trip = repository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());

            this.repository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("{id}/confirm")
    public ResponseEntity<Trip> tripConfirm(@PathVariable UUID id) {
        Optional<Trip> trip = repository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            rawTrip.setConfirmed(true);

            participantService.triggerConfirmationEmailToParticipants(rawTrip.getId());

            this.repository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> invite(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload) {
        Optional<Trip> trip = repository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            ParticipantCreateResponse response = participantService.registerParticipantToEvent(payload.email(), rawTrip);

            if (rawTrip.isConfirmed()) {
                participantService.triggerConfirmationEmailToParticipant(payload.email());
            }

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("{id}/participants")
    public ResponseEntity<List<ParticipantData>> participants(@PathVariable UUID id) {
        List<ParticipantData> participants = participantService.getAllParticipants(id);

        return ResponseEntity.ok(participants);
    }

    @PostMapping("{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayload payload) {
        Optional<Trip> trip = repository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            ActivityResponse response = activityService.saveActivity(payload, rawTrip);

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }
}
