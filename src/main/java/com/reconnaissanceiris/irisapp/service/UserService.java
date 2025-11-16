package com.reconnaissanceiris.irisapp.service;

import com.reconnaissanceiris.irisapp.model.Users;

import java.util.List;
import java.util.Optional;

public interface UserService {
    /*
    Interface pour la gestion CRUD des utilisateurs
     */
    Users saveUsers(Users user);
    Optional<Users> findByEmail(String email);
    List<Users> getAllUsers();
    void deleteUser(Long id);
}
