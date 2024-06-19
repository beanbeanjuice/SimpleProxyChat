package com.beanbeanjuice.simpleproxychat.utility.status;

import lombok.Getter;

import java.util.Optional;

public class ServerStatus {

    @Getter private Boolean status;  // Object wrapper to use Object#equals to detect state change.
    private Boolean previousStatus;  // Object wrapper to use Object#equals to detect state change.
    private int onlineCount = 0;
    private int offlineCount = 0;

    private static final int COUNT_UNTIL_UPDATE = 5;

    public ServerStatus() { }
    public ServerStatus(boolean initialStatus) {
        this.status = initialStatus;
        this.previousStatus = initialStatus;
    }

    private void resetCount() {
        onlineCount = 0;
        offlineCount = 0;
    }

    public Optional<Boolean> updateStatus(Boolean newStatus) {
        if (newStatus.equals(this.status)) return Optional.empty();  // Do nothing if no state change.
        if (!newStatus.equals(this.previousStatus)) resetCount();  // This means a state change has occurred.
        this.previousStatus = newStatus;

        int count = newStatus ? ++this.onlineCount : ++this.offlineCount;
        if (count < COUNT_UNTIL_UPDATE) return Optional.empty();  // Do nothing if conditions are not met.

        resetCount();  // Conditions are met. Reset.
        this.status = newStatus;
        return Optional.of(this.status);
    }

}
