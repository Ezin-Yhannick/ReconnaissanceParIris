package com.reconnaissanceiris.irisapp.service.impl;

import com.reconnaissanceiris.irisapp.service.IrisTraitementService;
import ij.ImagePlus;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Random;

@Service
public class IrisTraitementServiceImpl implements IrisTraitementService {


    @Override
    public String TraiteIrisImage(File imageFile) throws Exception {
        //Cette ligne permet de charger un image
        ImagePlus img = new ImagePlus(imageFile.getAbsolutePath());

        //On verifie ici si elle est nulle
        if(img == null || img.getProcessor() == null){
            throw new IllegalArgumentException("Impossible de charger l'image :" + imageFile.getName());
        }

        //Convertion en un Byte Processor pour pouvoir appliquer les filtres
            ImageProcessor ip = img.getProcessor().convertToByteProcessor();
            GaussianBlur gaussianBlur = new GaussianBlur();
            gaussianBlur.blurGaussian(ip, 2.0);

            //Simule un code binaire d'abord unique
            StringBuilder code = new StringBuilder();
            Random random = new Random();

            for(int i = 0; i < 256; i++){
                code.append(random.nextInt(2));
            }
            return code.toString();
    }
}

