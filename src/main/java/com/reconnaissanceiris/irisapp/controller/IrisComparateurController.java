package com.reconnaissanceiris.irisapp.controller;

import com.reconnaissanceiris.irisapp.model.DonneesIris;
import com.reconnaissanceiris.irisapp.model.Users;
import com.reconnaissanceiris.irisapp.service.IrisComparateurService;
import com.reconnaissanceiris.irisapp.service.IrisDonneesService;
import com.reconnaissanceiris.irisapp.service.IrisTraitementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;

@RestController
@RequestMapping("/api/iris/")
public class IrisComparateurController {

    @Autowired
    private IrisTraitementService irisTraitementService;

    @Autowired
    private IrisComparateurService irisComparateurService;

    @Autowired
    private IrisDonneesService irisDonneesService;

    @PostMapping("/compare")
    public ResponseEntity<?> compare(@RequestParam("userId") long userId,
                                     @RequestParam("image")MultipartFile file) throws Exception{
        // Fake user en mode Option A
        Users user = new Users();
        user.setId(userId);

        // Charger irisCode existant
        Optional<DonneesIris> data = irisDonneesService.findByUserId(user.getId());
        if (data.isEmpty()) {
            return ResponseEntity.badRequest().body("Aucun iris enrôlé pour cet utilisateur");
        }

        String storedCode = data.get().getCodeIris();

        // Sauvegarde temporaire du fichier
        File temp = File.createTempFile("iris_", ".png");
        file.transferTo(temp);

        // Iris code du nouvel échantillon
        String newCode = irisTraitementService.TraiteIrisImage(temp);

        // Comparaison
        double sim = irisComparateurService.computeSimilarite(storedCode, newCode);
        String decision = irisComparateurService.getResultatComparaison(sim, 0.85);

        return ResponseEntity.ok(
                String.format("Similarité : %.2f%% | Décision : %s", sim * 100, decision)
        );
    }
}
