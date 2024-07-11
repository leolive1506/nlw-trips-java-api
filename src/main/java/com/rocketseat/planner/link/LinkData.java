package com.rocketseat.planner.link;

import java.util.UUID;

public record LinkData(UUID id, String title, String url) {
    public LinkData(Link link) {
        this(link.getId(), link.getTitle(), link.getUrl());
    }
}
