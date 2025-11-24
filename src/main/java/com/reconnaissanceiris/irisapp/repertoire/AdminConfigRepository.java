package com.reconnaissanceiris.irisapp.repertoire;

import com.reconnaissanceiris.irisapp.model.AdminConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminConfigRepository extends JpaRepository<AdminConfig, Long> {
    
    // Récupérer la configuration admin (il n'y en a qu'une seule normalement)
    Optional<AdminConfig> findFirstByOrderByIdAsc();
    
    // Trouver par email
    Optional<AdminConfig> findByAdminEmail(String adminEmail);
}