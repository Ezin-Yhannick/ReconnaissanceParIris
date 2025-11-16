package com.reconnaissanceiris.irisapp.repertoire;

import com.reconnaissanceiris.irisapp.model.DonneesIris;
import com.reconnaissanceiris.irisapp.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IrisRepository extends JpaRepository<DonneesIris, Long> {
    Optional<DonneesIris> findByUser(Users user);
}
