package gestion.securelife.entity;

import gestion.securelife.entity.enums.ContratStatus;
import gestion.securelife.util.DateUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contrats")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment
    private Integer id;

    @Column(nullable = false, unique = true)
    private String numeroContrat;

    @Column(nullable = false)
    private String nom_complet;


    @Email
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Double primeBase;

    @Column(nullable = false)
    private Double primeAnnuelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContratStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;


    @PrePersist
    public void onCreate() {
        LocalDateTime now = DateUtils.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.status = ContratStatus.ACTIVE;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = DateUtils.now();
    }
}

