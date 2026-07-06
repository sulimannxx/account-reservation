package org.example.accountreservation.repository;

import java.util.UUID;
import org.example.accountreservation.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {

    boolean existsByMdmId(Long mdmId);
}
