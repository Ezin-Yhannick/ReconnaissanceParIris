package com.reconnaissanceiris.irisapp.repertoire;

import com.reconnaissanceiris.irisapp.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    // Trouver un utilisateur par email
    Users findByEmail(String email);

    // Vérifier si un email existe
    boolean existsByEmail(String email);

}