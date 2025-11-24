package com.reconnaissanceiris.irisapp.service.impl;

import com.reconnaissanceiris.irisapp.service.IrisTraitementService;
import ij.ImagePlus;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class IrisTraitementServiceImpl implements IrisTraitementService {

    private static final int CODE_LENGTH = 512; // Augmenté de 256 à 512 pour plus de précision
    private static final int REGIONS = 16; // Augmenté de 8 à 16
    private static final int SECTORS = 32; // Nombre de secteurs angulaires

    @Override
    public String TraiteIrisImage(File imageFile) throws Exception {
        // Charger l'image
        ImagePlus img = new ImagePlus(imageFile.getAbsolutePath());

        if (img == null || img.getProcessor() == null) {
            throw new IllegalArgumentException("Impossible de charger l'image : " + imageFile.getName());
        }

        // Conversion en ByteProcessor et application du filtre gaussien
        ImageProcessor ip = img.getProcessor().convertToByteProcessor();
        GaussianBlur gaussianBlur = new GaussianBlur();
        gaussianBlur.blurGaussian(ip, 1.5); // Réduction du flou pour garder plus de détails

        // Générer un code basé sur l'analyse de texture de l'iris
        StringBuilder code = new StringBuilder();

        int width = ip.getWidth();
        int height = ip.getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Calculer le rayon moyen de l'iris
        int maxRadius = Math.min(width, height) / 2;

        // Analyser l'iris en régions concentriques et secteurs angulaires
        for (int i = 0; i < CODE_LENGTH; i++) {
            // Déterminer la région et le secteur pour ce bit
            int region = i % REGIONS;
            int sector = (i / REGIONS) % SECTORS;

            // Calculer la position en coordonnées polaires
            double radius = (region + 1) * maxRadius / (double) REGIONS;
            double angle = sector * 2 * Math.PI / SECTORS;

            // Convertir en coordonnées cartésiennes
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));

            // Vérifier les limites
            x = Math.max(0, Math.min(width - 1, x));
            y = Math.max(0, Math.min(height - 1, y));

            // Analyser la texture locale avec plusieurs voisins (plus discriminant)
            int bit = analyzeLocalTexture(ip, x, y, width, height);
            code.append(bit);
        }

        return code.toString();
    }

    /**
     * Analyse la texture locale en comparant avec plusieurs voisins
     * Plus discriminant que la version précédente
     */
    private int analyzeLocalTexture(ImageProcessor ip, int x, int y, int width, int height) {
        int centerPixel = ip.getPixel(x, y);
        int score = 0;

        // Comparer avec 8 voisins directs (plus robuste)
        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

        for (int i = 0; i < 8; i++) {
            int nx = Math.max(0, Math.min(width - 1, x + dx[i]));
            int ny = Math.max(0, Math.min(height - 1, y + dy[i]));
            int neighborPixel = ip.getPixel(nx, ny);

            // Compter combien de voisins sont plus sombres
            if (centerPixel > neighborPixel) {
                score++;
            }
        }

        // Si la majorité des voisins sont plus sombres, retourner 1
        return score >= 4 ? 1 : 0;
    }
}