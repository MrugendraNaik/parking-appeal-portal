package com.vit.parkingappeal.web;

import com.vit.parkingappeal.model.AppealStatus;

/** One count tile on the reviewer dashboard. Plain getters so Thymeleaf can read it safely. */
public class StatusTile {

    private final AppealStatus status;
    private final long count;
    private final boolean active;

    public StatusTile(AppealStatus status, long count, boolean active) {
        this.status = status;
        this.count = count;
        this.active = active;
    }

    public AppealStatus getStatus() { return status; }
    public long getCount() { return count; }
    public boolean isActive() { return active; }
}
