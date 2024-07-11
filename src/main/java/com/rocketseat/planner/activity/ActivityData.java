package com.rocketseat.planner.activity;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityData(UUID id, String title, LocalDateTime occursAt) {
    public ActivityData(Activity activity) {
        this(activity.getId(), activity.getTitle(), activity.getOccursAt());
    }
}
