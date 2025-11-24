package com.reconnaissanceiris.irisapp.controller;

import com.reconnaissanceiris.irisapp.model.Users;
import com.reconnaissanceiris.irisapp.repertoire.UsersRepository;
import com.reconnaissanceiris.irisapp.service.AdminConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AdminConfigService adminConfigService;

    /**
     * Connexion Admin avec identifiants depuis la base de données
     * POST /api/auth/admin-login
     */
    @PostMapping("/admin-login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "Email et mot de passe requis");
                return ResponseEntity.status(400).body(response);
            }

            // Vérifier avec la BD au lieu de application.properties
            if (adminConfigService.verifyAdminCredentials(email, password)) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("email", email);
                userData.put("nom", "Administrateur");
                userData.put("prenom", "Système");
                userData.put("role", "admin");

                response.put("success", true);
                response.put("message", "Connexion admin réussie");
                response.put("user", userData);
                response.put("token", "admin-token-" + System.currentTimeMillis());

                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Email ou mot de passe incorrect");
                return ResponseEntity.status(401).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur serveur : " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Connexion classique (email + mot de passe) - Pour les utilisateurs avec mot de passe
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "Email et mot de passe requis");
                return ResponseEntity.status(400).body(response);
            }

            // Chercher l'utilisateur par email
            Users user = usersRepository.findByEmail(email).orElseThrow();

            if (user == null) {
                response.put("success", false);
                response.put("message", "Email ou mot de passe incorrect");
                return ResponseEntity.status(401).body(response);
            }

            // Vérifier le mot de passe (ATTENTION: en prod, utiliser BCrypt)
            if (!user.getMotDePasse().equals(password)) {
                response.put("success", false);
                response.put("message", "Email ou mot de passe incorrect");
                return ResponseEntity.status(401).body(response);
            }

            // Connexion réussie
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("nom", user.getNom());
            userData.put("prenom", user.getPrenom());
            userData.put("role", user.getRole());

            response.put("success", true);
            response.put("message", "Connexion réussie");
            response.put("user", userData);
            response.put("token", "user-token-" + user.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur serveur : " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Vérifier si un email existe déjà
     * GET /api/auth/check-email?email=xxx
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean exists = usersRepository.existsByEmail(email);

            response.put("exists", exists);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Déconnexion (côté serveur)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Déconnexion réussie");
        return ResponseEntity.ok(response);
    }
}