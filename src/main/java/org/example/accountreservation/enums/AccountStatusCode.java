package org.example.accountreservation.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum AccountStatusCode {
    NEW(true),
    IN_CREATION(true),
    CREATED(true),
    CANCELLED(false),
    CLOSED(false);

    private final boolean active;

    AccountStatusCode(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public static Set<String> activeNames() {
        return Arrays.stream(values())
                .filter(AccountStatusCode::isActive)
                .map(AccountStatusCode::name)
                .collect(Collectors.toSet());
    }
}
