package com.reconnaissanceiris.irisapp.controller;

import com.reconnaissanceiris.irisapp.model.DonneesIris;
import com.reconnaissanceiris.irisapp.model.Users;
import com.reconnaissanceiris.irisapp.repertoire.IrisRepository;
import com.reconnaissanceiris.irisapp.repertoire.UsersRepository;
import com.reconnaissanceiris.irisapp.service.IrisTraitementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    @Transactional // IMPORTANT : Rollback automatique en cas d'erreur
    public ResponseEntity<Map<String, Object>> enrollUser(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("email") String email,
            @RequestParam(value = "motDePasse", required = false) String motDePasse,
            @RequestParam(value = "role", defaultValue = "user") String role,
            @RequestParam("irisImage") MultipartFile irisImage) {

        Map<String, Object> response = new HashMap<>();
        File tempFile = null;

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

            // 3. TRAITER L'IRIS EN PREMIER (avant de créer l'utilisateur)
            tempFile = File.createTempFile("iris_enroll_", ".jpg");
            irisImage.transferTo(tempFile);

            System.out.println("📁 Fichier temporaire créé: " + tempFile.getAbsolutePath());
            System.out.println("📏 Taille fichier: " + tempFile.length() + " bytes");
            System.out.println("✅ Fichier existe: " + tempFile.exists());
            System.out.println("📖 Fichier lisible: " + tempFile.canRead());

            String codeIris;
            try {
                System.out.println("🔄 Début traitement iris...");
                codeIris = irisTraitementService.TraiteIrisImage(tempFile);
                System.out.println("✅ Traitement réussi!");
            } catch (Exception e) {
                System.err.println("❌ ERREUR TRAITEMENT: " + e.getMessage());
                e.printStackTrace();
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
                response.put("status", "error");
                response.put("message", "Erreur lors du traitement de l'image d'iris: " + e.getMessage());
                return ResponseEntity.status(400).body(response);
            }

            System.out.println("========== DEBUG ENROLEMENT ==========");
            System.out.println("Code iris généré (premiers 50 chars): " + codeIris.substring(0, Math.min(50, codeIris.length())));
            System.out.println("Longueur code: " + codeIris.length());
            System.out.println("=======================================");

            // 4. Vérifier si cet iris existe déjà dans la base
            if (irisRepository.existsByCodeIris(codeIris)) {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
                response.put("status", "error");
                response.put("message", "Cette image d'iris a déjà été enregistrée pour un autre utilisateur");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // 5. CRÉER L'UTILISATEUR (seulement si l'iris est valide et unique)
            Users user = Users.builder()
                    .nom(nom)
                    .prenom(prenom)
                    .email(email)
                    .motDePasse(motDePasse != null ? motDePasse : generateDefaultPassword())
                    .role(role)
                    .build();

            Users savedUser = usersRepository.save(user);
            System.out.println("✅ Utilisateur créé avec ID: " + savedUser.getId());

            // 6. ✅ CORRECTION : Sauvegarder l'image depuis tempFile (au lieu de irisImage)
            String fileName = UUID.randomUUID().toString() + "_" + prenom + "_" + nom + ".jpg";
            String filePath = saveImageFromTempFile(tempFile, fileName);
            System.out.println("✅ Image sauvegardée: " + filePath);

            // Supprimer le fichier temporaire
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
                System.out.println("🗑️ Fichier temporaire supprimé");
            }

            // 7. Créer l'enregistrement iris
            DonneesIris donneesIris = DonneesIris.builder()
                    .user(savedUser)
                    .cheminImage(filePath)
                    .codeIris(codeIris)
                    .dateenrollement(LocalDateTime.now())
                    .build();

            DonneesIris savedIris = irisRepository.save(donneesIris);
            System.out.println("✅ Iris enregistré avec ID: " + savedIris.getId());

            // 8. Réponse succès
            response.put("status", "success");
            response.put("message", "Utilisateur enrôlé avec succès");
            response.put("userId", savedUser.getId());
            response.put("irisId", savedIris.getId());
            response.put("enrollmentDate", savedIris.getDateenrollement());

            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            // Erreur de contrainte unique (code_iris déjà existant)
            System.err.println("❌ Erreur contrainte unique: " + e.getMessage());
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            response.put("status", "error");
            response.put("message", "Cette image d'iris a déjà été enregistrée pour un autre utilisateur");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            // Nettoyage en cas d'erreur
            System.err.println("❌ Erreur générale: " + e.getMessage());
            e.printStackTrace();
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            response.put("status", "error");
            response.put("message", "Erreur lors de l'enrôlement : " + e.getMessage());
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
     * Sauvegarder l'image depuis un fichier temporaire déjà existant
     * ✅ NOUVELLE MÉTHODE CORRIGÉE
     */
    private String saveImageFromTempFile(File tempFile, String fileName) throws IOException {
        // Créer le dossier s'il n'existe pas
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            System.out.println("📁 Dossier créé: " + uploadDir.getAbsolutePath());
        }

        // Copier le fichier temporaire vers le dossier permanent
        Path targetPath = Paths.get(UPLOAD_DIR + fileName);
        Files.copy(tempFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }

    /**
     * Sauvegarder l'image sur le disque (ANCIENNE MÉTHODE - peut être supprimée)
     * ⚠️ Cette méthode n'est plus utilisée mais je la laisse au cas où
     */
    @Deprecated
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
     * Générer un mot de passe par défaut
     */
    private String generateDefaultPassword() {
        return "Iris" + UUID.randomUUID().toString().substring(0, 8);
    }
}