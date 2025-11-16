package com.reconnaissanceiris.irisapp.controller;

import com.reconnaissanceiris.irisapp.model.DonneesIris;
import com.reconnaissanceiris.irisapp.model.Users;
import com.reconnaissanceiris.irisapp.service.IrisDonneesService;
import com.reconnaissanceiris.irisapp.service.IrisTraitementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/*
Il s'agit ici d'un controller pour permettre la connexion avec le front end pour l'appelation de methode de traitement et de stockage dans la base de données
 */
@RestController
@RequestMapping("/api/iris/")
public class IrisEnrollController {

    @Autowired
    private IrisTraitementService irisTraitementService;

    @Autowired
    private IrisDonneesService irisDonneesService;

    @PostMapping("/enroll")
    public ResponseEntity<?> enroll(@RequestParam("userId") Long userId,
                                    @RequestParam("image") MultipartFile file) throws Exception{

        //On créée d'abord un faux utilisateur
        Users user = new Users();
        user.setId(userId);
        user.setNom("testUser");

        //Sauvegarde temporaire du fichier
        File temp = File.createTempFile("iris", ".png");
        file.transferTo(temp);

        //Generation de code
        String irisCode = irisTraitementService.TraiteIrisImage(temp);

        //Sauvegarde en base
        DonneesIris saved = irisDonneesService.enrollIris(user, temp);

        return ResponseEntity.ok(saved);
    }
}
