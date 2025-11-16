package com.reconnaissanceiris.irisapp.service.impl;

import com.reconnaissanceiris.irisapp.service.IrisComparateurService;
import org.springframework.stereotype.Service;

@Service
public class IrisComparateurServiceImpl implements IrisComparateurService {
    @Override
    public double computeHammingDistance(String code1, String code2) {

        int distance = 0;
        if (code1.length() != code2.length()) {
            throw new IllegalArgumentException("Les codes doivent avoir la même taille!");
        }

            for (int i = 0; i < code1.length(); i++) {
                if (code1.charAt(i) != code2.charAt(i)) {
                    distance++;
                }
            }

        return (double) distance / code1.length();
    }

    @Override
    public double computeSimilarite(String code1, String code2) {
        return 1 - computeHammingDistance(code1, code2);
    }

    @Override
    public String getResultatComparaison(double similarite, double threshold) {
        return (similarite >= threshold)
                ? "Correspondance trouvée "
                : "Aucune correspondance ";
    }
}
