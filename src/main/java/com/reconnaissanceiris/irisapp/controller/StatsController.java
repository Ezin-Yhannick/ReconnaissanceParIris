package com.reconnaissanceiris.irisapp.controller;

import com.reconnaissanceiris.irisapp.repertoire.UsersRepository;
import com.reconnaissanceiris.irisapp.repertoire.IrisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatsController {
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private IrisRepository irisRepository;
    
    /**
     * Récupérer toutes les statistiques pour le dashboard admin
     * GET /api/stats/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Nombre total d'utilisateurs
            long totalUsers = usersRepository.count();
            
            // Nombre total d'iris enregistrés
            long totalIrisRecords = irisRepository.count();
            
            // TODO: Ajouter les authentifications depuis une table de logs
            // Pour l'instant, valeurs par défaut
            int totalAuthentications = 0; // À implémenter avec table auth_logs
            double successRate = totalIrisRecords > 0 ? 98.5 : 0.0;
            
            stats.put("totalUsers", totalUsers);
            stats.put("totalIrisRecords", totalIrisRecords);
            stats.put("authentications", totalAuthentications);
            stats.put("successRate", successRate);
            stats.put("status", "success");
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Récupérer les stats d'un utilisateur spécifique
     * GET /api/stats/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long userId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            boolean userExists = usersRepository.existsById(userId);
            
            if (!userExists) {
                stats.put("status", "error");
                stats.put("message", "Utilisateur non trouvé");
                return ResponseEntity.status(404).body(stats);
            }
            
            // TODO: Statistiques spécifiques à l'utilisateur
            stats.put("userId", userId);
            stats.put("hasIrisEnrolled", irisRepository.existsByUserId(userId));
            stats.put("status", "success");
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}