package com.reconnaissanceiris.irisapp.controller;

import com.reconnaissanceiris.irisapp.model.Users;
import com.reconnaissanceiris.irisapp.repertoire.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UsersController {

    @Autowired
    private UsersRepository usersRepository;

    /**
     * Récupérer tous les utilisateurs
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<Users> users = usersRepository.findAll();

            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("total", users.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Vérifier si un email existe déjà
     * GET /api/users/check-email?email=xxx@xxx.com
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailExists(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean exists = usersRepository.existsByEmail(email);

            response.put("exists", exists);
            response.put("email", email);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Récupérer un utilisateur par ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            Optional<Users> user = usersRepository.findById(id);

            Map<String, Object> response = new HashMap<>();

            if (user.isPresent()) {
                response.put("user", user.get());
                response.put("status", "success");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Utilisateur non trouvé");
                return ResponseEntity.status(404).body(response);
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Mettre à jour un utilisateur
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody Users updatedUser) {

        try {
            Optional<Users> existingUser = usersRepository.findById(id);

            Map<String, Object> response = new HashMap<>();

            if (!existingUser.isPresent()) {
                response.put("status", "error");
                response.put("message", "Utilisateur non trouvé");
                return ResponseEntity.status(404).body(response);
            }

            Users user = existingUser.get();

            // Mise à jour des champs
            if (updatedUser.getNom() != null) {
                user.setNom(updatedUser.getNom());
            }
            if (updatedUser.getPrenom() != null) {
                user.setPrenom(updatedUser.getPrenom());
            }
            if (updatedUser.getEmail() != null) {
                user.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getRole() != null) {
                user.setRole(updatedUser.getRole());
            }

            Users savedUser = usersRepository.save(user);

            response.put("user", savedUser);
            response.put("status", "success");
            response.put("message", "Utilisateur mis à jour avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Supprimer un utilisateur
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            Map<String, Object> response = new HashMap<>();

            if (!usersRepository.existsById(id)) {
                response.put("status", "error");
                response.put("message", "Utilisateur non trouvé");
                return ResponseEntity.status(404).body(response);
            }

            usersRepository.deleteById(id);

            response.put("status", "success");
            response.put("message", "Utilisateur supprimé avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Rechercher des utilisateurs par email
     * GET /api/users/search?email=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(@RequestParam String email) {
        try {
            // Vous devrez ajouter cette méthode dans UsersRepository
            // List<Users> users = usersRepository.findByEmailContaining(email);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Recherche non implémentée - ajouter findByEmailContaining dans UsersRepository");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}