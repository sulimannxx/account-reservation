package org.example.accountreservation.repository;

import org.example.accountreservation.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStatusRepository extends JpaRepository<AccountStatus, Integer> {
}
