package com.reconnaissanceiris.irisapp.service;

import com.reconnaissanceiris.irisapp.model.AdminConfig;
import com.reconnaissanceiris.irisapp.repertoire.AdminConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AdminConfigService {

    @Autowired
    private AdminConfigRepository adminConfigRepository;

    /**
     * Récupérer la configuration admin
     */
    public AdminConfig getAdminConfig() {
        return adminConfigRepository.findFirstByOrderByIdAsc()
                .orElse(null);
    }

    /**
     * Vérifier les identifiants admin
     */
    public boolean verifyAdminCredentials(String email, String password) {
        Optional<AdminConfig> config = adminConfigRepository.findFirstByOrderByIdAsc();
        
        if (config.isPresent()) {
            AdminConfig adminConfig = config.get();
            return adminConfig.getAdminEmail().equals(email) 
                   && adminConfig.getAdminPassword().equals(password);
        }
        
        return false;
    }

    /**
     * Mettre à jour les identifiants admin
     */
    public AdminConfig updateAdminCredentials(String currentPassword, String newEmail, String newPassword) throws Exception {
        Optional<AdminConfig> configOpt = adminConfigRepository.findFirstByOrderByIdAsc();
        
        if (!configOpt.isPresent()) {
            throw new Exception("Configuration admin non trouvée");
        }
        
        AdminConfig config = configOpt.get();
        
        // Vérifier le mot de passe actuel
        if (!config.getAdminPassword().equals(currentPassword)) {
            throw new Exception("Mot de passe actuel incorrect");
        }
        
        // Mettre à jour
        if (newEmail != null && !newEmail.isEmpty()) {
            config.setAdminEmail(newEmail);
        }
        
        if (newPassword != null && !newPassword.isEmpty()) {
            config.setAdminPassword(newPassword);
        }
        
        config.setLastUpdated(LocalDateTime.now());
        
        return adminConfigRepository.save(config);
    }

    /**
     * Créer la configuration admin initiale (si elle n'existe pas)
     */
    public AdminConfig createInitialAdminConfig(String email, String password) {
        Optional<AdminConfig> existing = adminConfigRepository.findFirstByOrderByIdAsc();
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        AdminConfig config = AdminConfig.builder()
                .adminEmail(email)
                .adminPassword(password)
                .createdAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();
        
        return adminConfigRepository.save(config);
    }
    public AdminConfig updateAdminProfile(String nom, String prenom) {
        AdminConfig config = getAdminConfig();

        if (config == null) {
            throw new RuntimeException("Configuration admin non trouvée");
        }

        config.setNom(nom);
        config.setPrenom(prenom);
        config.setLastUpdated(java.time.LocalDateTime.now());

        return adminConfigRepository.save(config);
    }
}
