package com.reconnaissanceiris.irisapp.controller;

import com.reconnaissanceiris.irisapp.model.DonneesIris;
import com.reconnaissanceiris.irisapp.model.Users;
import com.reconnaissanceiris.irisapp.repertoire.IrisRepository;
import com.reconnaissanceiris.irisapp.service.IrisComparateurService;
import com.reconnaissanceiris.irisapp.service.IrisTraitementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iris")
@CrossOrigin(origins = "*")
public class IrisAuthenticationController {

    @Autowired
    private IrisRepository irisRepository;

    @Autowired
    private IrisTraitementService irisTraitementService;

    @Autowired
    private IrisComparateurService irisComparateurService;

    /**
     * Authentification par reconnaissance d'iris
     * POST /api/iris/authenticate
     */
    @PostMapping("/authenticate")
    public ResponseEntity<Map<String, Object>> authenticateByIris(
            @RequestParam("irisImage") MultipartFile irisImage) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Valider l'image
            if (irisImage.isEmpty()) {
                response.put("success", false);
                response.put("message", "Image de l'iris requise");
                return ResponseEntity.status(400).body(response);
            }

            // 2. Traiter l'image et extraire le code iris
            String uploadedIrisCode = processIrisForAuthentication(irisImage);

            // 3. Récupérer tous les iris de la BD
            List<DonneesIris> allIrisRecords = irisRepository.findAll();

            if (allIrisRecords.isEmpty()) {
                response.put("success", false);
                response.put("message", "Aucun iris enregistré dans le système");
                return ResponseEntity.status(404).body(response);
            }

            // 4. Comparer avec tous les iris
            DonneesIris matchedIris = null;
            double bestMatchScore = 0.0;

            System.out.println("========== DEBUG AUTHENTIFICATION ==========");
            System.out.println("Code iris uploadé (premiers 50 chars): " + uploadedIrisCode.substring(0, Math.min(50, uploadedIrisCode.length())));
            System.out.println("Longueur code uploadé: " + uploadedIrisCode.length());
            System.out.println("Nombre d'iris en BD: " + allIrisRecords.size());

            for (DonneesIris irisRecord : allIrisRecords) {
                String storedCode = irisRecord.getCodeIris();
                System.out.println("--- Comparaison avec utilisateur ID: " + irisRecord.getUser().getId());
                System.out.println("Code stocké (premiers 50 chars): " + (storedCode != null ? storedCode.substring(0, Math.min(50, storedCode.length())) : "NULL"));
                System.out.println("Longueur code stocké: " + (storedCode != null ? storedCode.length() : 0));

                double similarity = compareIrisCodes(uploadedIrisCode, storedCode);
                System.out.println("Similarité calculée: " + (similarity * 100) + "%");

                // Seuil de correspondance : 85%
                if (similarity > bestMatchScore && similarity >= 0.85) {
                    bestMatchScore = similarity;
                    matchedIris = irisRecord;
                }
            }
            System.out.println("Meilleur score final: " + (bestMatchScore * 100) + "%");
            System.out.println("============================================");

            // 5. Si match trouvé
            if (matchedIris != null) {
                Users user = matchedIris.getUser();

                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("email", user.getEmail());
                userData.put("nom", user.getNom());
                userData.put("prenom", user.getPrenom());
                userData.put("role", user.getRole());

                response.put("success", true);
                response.put("message", "Authentification réussie");
                response.put("user", userData);
                response.put("matchRate", bestMatchScore * 100); // En pourcentage
                response.put("token", "user-token-" + user.getId() + "-" + System.currentTimeMillis());

                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Iris non reconnu. Aucune correspondance trouvée.");
                response.put("bestScore", bestMatchScore * 100);
                return ResponseEntity.status(401).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de l'authentification : " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Traiter l'image iris pour l'authentification
     */
    private String processIrisForAuthentication(MultipartFile file) throws Exception {
        // Sauvegarder temporairement le fichier
        File tempFile = File.createTempFile("iris_auth_", ".jpg");
        file.transferTo(tempFile);

        try {
            // Utiliser le vrai service de traitement
            return irisTraitementService.TraiteIrisImage(tempFile);
        } finally {
            // Supprimer le fichier temporaire
            tempFile.delete();
        }
    }

    /**
     * Comparer deux codes iris et retourner un score de similarité
     */
    private double compareIrisCodes(String code1, String code2) {
        if (code1 == null || code2 == null) {
            return 0.0;
        }

        try {
            // Utiliser le service de comparaison avec distance de Hamming
            return irisComparateurService.computeSimilarite(code1, code2);
        } catch (IllegalArgumentException e) {
            System.out.println("ERREUR comparaison: " + e.getMessage());
            // Si les codes n'ont pas la même taille
            if (code1.equals(code2)) {
                return 1.0;
            }
            return 0.0;
        }
    }
}