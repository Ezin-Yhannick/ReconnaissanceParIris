package com.reconnaissanceiris.irisapp.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "utilisateurs")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = true)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String mot_de_passe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


}
