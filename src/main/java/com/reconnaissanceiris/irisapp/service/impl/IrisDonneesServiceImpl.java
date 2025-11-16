package com.reconnaissanceiris.irisapp.service.impl;

import com.reconnaissanceiris.irisapp.model.DonneesIris;
import com.reconnaissanceiris.irisapp.model.Users;
import com.reconnaissanceiris.irisapp.repertoire.IrisRepository;
import com.reconnaissanceiris.irisapp.service.IrisDonneesService;
import com.reconnaissanceiris.irisapp.service.IrisTraitementService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
public class IrisDonneesServiceImpl implements IrisDonneesService {

    private final IrisRepository donneesIrisRepository;
    private final IrisTraitementService irisTraitementService;

    public IrisDonneesServiceImpl(IrisRepository donneesIrisRepository, IrisTraitementService irisTraitementService) {
        this.donneesIrisRepository = donneesIrisRepository;
        this.irisTraitementService = irisTraitementService;
    }

    @Override
    public DonneesIris enrollIris(Users user, File irisImage) throws Exception {
        //1 Traitement de l'image d'iris
        String irisCode = irisTraitementService.TraiteIrisImage(irisImage);

        //2 Creation et sauvegarde de l'objet DonneesIris
        DonneesIris data = new DonneesIris();
        data.setUser(user);
        data.setCodeIris(irisCode);
        data.setCheminImage(irisImage.getAbsolutePath());
        data.setDateenrollement(java.time.LocalDateTime.now());

        return donneesIrisRepository.save(data);
    }

    @Override
    public Optional<DonneesIris> findByUser(Users user) {
        return donneesIrisRepository.findByUser(user);
    }
}
