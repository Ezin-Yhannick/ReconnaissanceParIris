package com.reconnaissanceiris.irisapp.service.impl;

import com.reconnaissanceiris.irisapp.service.IrisTraitementService;
import ij.ImagePlus;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class IrisTraitementServiceImpl implements IrisTraitementService {

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
        gaussianBlur.blurGaussian(ip, 2.0);

        // Générer un code basé sur les pixels de l'image (déterministe)
        StringBuilder code = new StringBuilder();

        int width = ip.getWidth();
        int height = ip.getHeight();

        // Échantillonner 256 points de l'image pour créer le code iris
        for (int i = 0; i < 256; i++) {
            int x = (i * width / 256) % width;
            int y = (i * height / 256) % height;
            int pixel = ip.getPixel(x, y);

            // Convertir la valeur du pixel en bit (0 ou 1)
            code.append(pixel > 127 ? "1" : "0");
        }

        return code.toString();
    }
}