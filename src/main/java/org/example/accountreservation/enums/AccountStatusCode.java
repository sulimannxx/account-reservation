package org.example.accountreservation.enums;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum AccountStatusCode {
    NEW,
    IN_CREATION,
    CREATED,
    CANCELLED,
    CLOSED;

    private static final Set<AccountStatusCode> NOT_ACTIVE_STATUSES = EnumSet.of(CANCELLED, CLOSED);

    public boolean isActive() {
        return !NOT_ACTIVE_STATUSES.contains(this);
    }

    public static Set<String> activeNames() {
        return EnumSet.complementOf(EnumSet.copyOf(NOT_ACTIVE_STATUSES)).stream()
                .filter(AccountStatusCode::isActive)
                .map(AccountStatusCode::name)
                .collect(Collectors.toSet());
    }
}
