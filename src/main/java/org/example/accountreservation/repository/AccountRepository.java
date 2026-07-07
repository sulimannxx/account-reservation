package org.example.accountreservation.repository;

import java.util.Collection;
import java.util.UUID;
import org.example.accountreservation.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByClient_IdAndStatus_NameIn(UUID clientId, Collection<String> statusNames);
}
