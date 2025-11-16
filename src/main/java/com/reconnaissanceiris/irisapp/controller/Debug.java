package com.reconnaissanceiris.irisapp.controller;

import com.reconnaissanceiris.irisapp.service.IrisTraitementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public class Debug {
    @Autowired
    private IrisTraitementService irisTraitementService;
    @PostMapping("/debug")
    public ResponseEntity<?> debug(@RequestParam("image") MultipartFile file) throws Exception {

        File temp = File.createTempFile("iris_", ".png");
        file.transferTo(temp);

        String code = irisTraitementService.TraiteIrisImage(temp);

        return ResponseEntity.ok(code);
    }

}
