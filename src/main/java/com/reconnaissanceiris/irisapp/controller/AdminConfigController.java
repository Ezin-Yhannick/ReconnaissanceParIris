package com.reconnaissanceiris.irisapp.controller;

import com.reconnaissanceiris.irisapp.model.AdminConfig;
import com.reconnaissanceiris.irisapp.service.AdminConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminConfigController {

    @Autowired
    private AdminConfigService adminConfigService;

    /**
     * Récupérer les informations admin (sans le mot de passe)
     * GET /api/admin/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getAdminProfile() {
        try {
            AdminConfig config = adminConfigService.getAdminConfig();

            if (config == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Configuration admin non trouvée");
                return ResponseEntity.status(404).body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("email", config.getAdminEmail());
            response.put("nom", config.getNom() != null ? config.getNom() : "Admin");
            response.put("prenom", config.getPrenom() != null ? config.getPrenom() : "Super");
            response.put("createdAt", config.getCreatedAt());
            response.put("lastUpdated", config.getLastUpdated());
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
     * Mettre à jour le profil admin (nom et prénom)
     * PUT /api/admin/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateAdminProfile(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String nom = request.get("nom");
            String prenom = request.get("prenom");

            if (nom == null || nom.isEmpty() || prenom == null || prenom.isEmpty()) {
                response.put("success", false);
                response.put("message", "Nom et prénom requis");
                return ResponseEntity.status(400).body(response);
            }

            AdminConfig updated = adminConfigService.updateAdminProfile(nom, prenom);

            response.put("success", true);
            response.put("message", "Profil mis à jour avec succès");
            response.put("nom", updated.getNom());
            response.put("prenom", updated.getPrenom());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Mettre à jour les identifiants admin
     * PUT /api/admin/update credentials
     */
    @PutMapping("/update-credentials")
    public ResponseEntity<Map<String, Object>> updateCredentials(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentPassword = request.get("currentPassword");
            String newEmail = request.get("newEmail");
            String newPassword = request.get("newPassword");
            
            if (currentPassword == null || currentPassword.isEmpty()) {
                response.put("success", false);
                response.put("message", "Mot de passe actuel requis");
                return ResponseEntity.status(400).body(response);
            }
            
            AdminConfig updated = adminConfigService.updateAdminCredentials(
                currentPassword, 
                newEmail, 
                newPassword
            );
            
            response.put("success", true);
            response.put("message", "Identifiants mis à jour avec succès");
            response.put("newEmail", updated.getAdminEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * Initialiser la configuration admin (à appeler une seule fois)
     * POST /api/admin/init
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initAdminConfig(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "Email et mot de passe requis");
                return ResponseEntity.status(400).body(response);
            }
            
            AdminConfig config = adminConfigService.createInitialAdminConfig(email, password);
            
            response.put("success", true);
            response.put("message", "Configuration admin créée");
            response.put("email", config.getAdminEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}