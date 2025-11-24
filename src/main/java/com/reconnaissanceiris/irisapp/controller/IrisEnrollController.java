package com.reconnaissanceiris.irisapp.controller;

import com.reconnaissanceiris.irisapp.model.DonneesIris;
import com.reconnaissanceiris.irisapp.model.Users;
import com.reconnaissanceiris.irisapp.repertoire.IrisRepository;
import com.reconnaissanceiris.irisapp.repertoire.UsersRepository;
import com.reconnaissanceiris.irisapp.service.IrisTraitementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/iris")
@CrossOrigin(origins = "*")
public class IrisEnrollController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private IrisRepository irisRepository;

    @Autowired
    private IrisTraitementService irisTraitementService;

    // Dossier de stockage des images iris
    private static final String UPLOAD_DIR = "uploads/iris/";

    /**
     * Enrôler un nouvel utilisateur avec son iris
     * POST /api/iris/enroll
     */
    @PostMapping("/enroll")
    public ResponseEntity<Map<String, Object>> enrollUser(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("email") String email,
            @RequestParam(value = "motDePasse", required = false) String motDePasse,
            @RequestParam(value = "role", defaultValue = "user") String role,
            @RequestParam("irisImage") MultipartFile irisImage) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Vérifier que l'email n'existe pas déjà
            if (usersRepository.existsByEmail(email)) {
                response.put("status", "error");
                response.put("message", "Un utilisateur avec cet email existe déjà");
                return ResponseEntity.status(400).body(response);
            }

            // 2. Valider l'image
            if (irisImage.isEmpty()) {
                response.put("status", "error");
                response.put("message", "L'image de l'iris est requise");
                return ResponseEntity.status(400).body(response);
            }

            // 3. Créer l'utilisateur
            Users user = Users.builder()
                    .nom(nom)
                    .prenom(prenom)
                    .email(email)
                    .motDePasse(motDePasse != null ? motDePasse : generateDefaultPassword())
                    .role(role)
                    .build();

            Users savedUser = usersRepository.save(user);

            // 4. Sauvegarder l'image sur le disque
            String fileName = UUID.randomUUID().toString() + "_" + irisImage.getOriginalFilename();
            String filePath = saveImageToFile(irisImage, fileName);

            // 5. Traiter l'iris avec IrisTraitementService
            String codeIris = processIrisImage(irisImage);

            System.out.println("========== DEBUG ENROLEMENT ==========");
            System.out.println("Code iris généré (premiers 50 chars): " + codeIris.substring(0, Math.min(50, codeIris.length())));
            System.out.println("Longueur code: " + codeIris.length());
            System.out.println("=======================================");

            // 6. Créer l'enregistrement iris
            DonneesIris donneesIris = DonneesIris.builder()
                    .user(savedUser)
                    .cheminImage(filePath)
                    .codeIris(codeIris)
                    .dateenrollement(LocalDateTime.now())
                    .build();

            DonneesIris savedIris = irisRepository.save(donneesIris);

            // 7. Réponse succès
            response.put("status", "success");
            response.put("message", "Utilisateur enrôlé avec succès");
            response.put("userId", savedUser.getId());
            response.put("irisId", savedIris.getId());
            response.put("enrollmentDate", savedIris.getDateenrollement());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erreur lors de l'enrôlement : " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Récupérer tous les enregistrements iris
     * GET /api/iris/records
     */
    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getAllIrisRecords() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("records", irisRepository.findAll());
            response.put("total", irisRepository.count());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Récupérer l'iris d'un utilisateur spécifique
     * GET /api/iris/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getIrisByUserId(@PathVariable Long userId) {
        try {
            Map<String, Object> response = new HashMap<>();

            DonneesIris iris = irisRepository.findByUserId(userId);

            if (iris == null) {
                response.put("status", "error");
                response.put("message", "Aucun iris trouvé pour cet utilisateur");
                return ResponseEntity.status(404).body(response);
            }

            response.put("iris", iris);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Sauvegarder l'image sur le disque
     */
    private String saveImageToFile(MultipartFile file, String fileName) throws IOException {
        // Créer le dossier s'il n'existe pas
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Sauvegarder le fichier
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.write(filePath, file.getBytes());

        return filePath.toString();
    }

    /**
     * Traiter l'image iris avec IrisTraitementService
     */
    private String processIrisImage(MultipartFile file) throws Exception {
        // Sauvegarder temporairement le fichier
        File tempFile = File.createTempFile("iris_enroll_", ".jpg");
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
     * Générer un mot de passe par défaut
     */
    private String generateDefaultPassword() {
        return "Iris" + UUID.randomUUID().toString().substring(0, 8);
    }
}