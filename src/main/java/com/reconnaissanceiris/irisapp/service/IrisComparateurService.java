package com.reconnaissanceiris.irisapp.service;

public interface IrisComparateurService {
    /*
    Service de comparaison des iris d'une personne avec ceux de la base de données
     */

    double computeHammingDistance(String code1, String code2); // Fonction pour calculer la distance de Hamming des deux iris
    double computeSimilarite(String code1, String code2); //Fonction pour voir la similarité des deux codes d'iris
    String getResultatComparaison(double similarite, double thresold);
}
