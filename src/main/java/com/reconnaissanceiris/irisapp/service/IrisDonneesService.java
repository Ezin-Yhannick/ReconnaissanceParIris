package com.reconnaissanceiris.irisapp.service;

import com.reconnaissanceiris.irisapp.model.DonneesIris;
import com.reconnaissanceiris.irisapp.model.Users;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;


public interface IrisDonneesService {
    /*
    Interface pour permettre l'enrollement des iris dans la BDD
     */
    /*
    @param user l'utilisateur à enroler
    @param irisImage l'image de l'iris
    @return les données d'iris sauvegarder
    @throws les exceptions en cas d'erreur de traitement
     */
    DonneesIris enrollIris(Users user, File irisImage) throws Exception;

    /*
    *
    * Rechercher les données l'iris liees a un utilisateur
    * @param user l'utilisateur dont on veut les donnees de l'iris
    * @return les donnees si elles existent
     */
    Optional<DonneesIris> findByUser(Users user);
}
