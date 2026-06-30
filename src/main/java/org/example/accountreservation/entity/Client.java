package org.example.accountreservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name")
    private String fullName;

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

    @Column(name = "mdm_code")
    private Long mdmCode;
}
