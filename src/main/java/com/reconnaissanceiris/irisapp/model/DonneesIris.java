package com.reconnaissanceiris.irisapp.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "donnéesiris")
public class DonneesIris {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties("donneesIris")
    private Users user;

    @Column(name = "code_iris", columnDefinition = "LONGTEXT", unique = true)
    private String codeIris;

    @Column(name = "chemin_image")
    private String cheminImage;

    @Column(name = "dateenrollement")
    private LocalDateTime dateenrollement;
}