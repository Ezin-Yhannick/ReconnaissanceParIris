package com.reconnaissanceiris.irisapp.service;

import java.io.File;

public interface IrisTraitementService {
    /**
     * Traite une image d'iris et retourne un code encodé
     * @param imageFile Fichier image de l'iris
     * @return code binaire reprsentant l'iris
     * @throws Exception si echec du traitement
     */

    String TraiteIrisImage(File imageFile) throws Exception;
}
