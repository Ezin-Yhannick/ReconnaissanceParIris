package com.reconnaissanceiris.irisapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "donnéesiris")
public class DonneesIris {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Setter
    @Getter
    @Lob
    private String CodeIris;

    @Setter
    private String CheminImage;

    @Setter
    private LocalDateTime dateenrollement;

}
