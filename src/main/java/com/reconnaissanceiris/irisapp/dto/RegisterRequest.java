package com.reconnaissanceiris.irisapp.dto;

public record RegisterRequest(String email, String nom, String prenom, String mot_de_passe, String role) {
}
