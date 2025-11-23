package com.reconnaissanceiris.irisapp.repertoire;

import com.reconnaissanceiris.irisapp.model.DonneesIris;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IrisRepository extends JpaRepository<DonneesIris, Long> {

    // Trouver l'iris d'un utilisateur par son ID
    DonneesIris findByUserId(Long userId);

    // Vérifier si un utilisateur a un iris enregistré
    boolean existsByUserId(Long userId);
}