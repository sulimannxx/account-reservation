package org.example.accountreservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.example.accountreservation.enums.ClientStatus;

@Entity
@Getter
@Setter
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "mdm_id")
    private Long mdmId;

    @Column(name = "first_name", length = 128)
    private String firstName;

    @Column(name = "last_name", length = 128)
    private String lastName;

    @Column(name = "middle_name", length = 128)
    private String middleName;

    @Column(length = 128)
    private String citizenship;

    @Column(name = "client_type", length = 64)
    private String clientType;

    @Column(name = "document_number", length = 64)
    private String documentNumber;

    @Column(name = "document_series", length = 64)
    private String documentSeries;

    @Column(name = "document_type", length = 64)
    private String documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ClientStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        status = status == null ? ClientStatus.ACTIVE : status;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
