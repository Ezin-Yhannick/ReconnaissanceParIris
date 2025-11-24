package com.reconnaissanceiris.irisapp.repertoire;

import com.reconnaissanceiris.irisapp.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    @Override
    Optional<Users> findById(Long aLong);

    // Trouver un utilisateur par email
    Optional<Users> findByEmail(String email);

    // Vérifier si un email existe
    boolean existsByEmail(String email);

}