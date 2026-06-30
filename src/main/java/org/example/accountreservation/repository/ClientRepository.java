package org.example.accountreservation.repository;

import java.util.UUID;
import org.example.accountreservation.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, UUID> {
}
