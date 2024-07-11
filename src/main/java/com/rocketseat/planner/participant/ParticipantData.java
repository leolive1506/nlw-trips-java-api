package com.rocketseat.planner.participant;

public record ParticipantData(String name, String email, boolean isConfirmed) {
    public ParticipantData(Participant participant) {
        this(participant.getName(), participant.getEmail(), participant.isConfirmed());
    }
}
