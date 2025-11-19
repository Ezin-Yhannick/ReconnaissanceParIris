package com.reconnaissanceiris.irisapp.controller;

import com.reconnaissanceiris.irisapp.config.JwtUtil;
import com.reconnaissanceiris.irisapp.dto.AuthRequest;
import com.reconnaissanceiris.irisapp.dto.AuthResponse;
import com.reconnaissanceiris.irisapp.dto.RegisterRequest;
import com.reconnaissanceiris.irisapp.model.Role;
import com.reconnaissanceiris.irisapp.model.Users;
import com.reconnaissanceiris.irisapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// L'IMPORT CLÉ DE SPRING SECURITY POUR LA GESTION DES ERREURS D'AUTHENTIFICATION
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

// Suppression des imports inutiles:
// import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
// import org.apache.tomcat.websocket.AuthenticationException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private  final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        if(userService.existsByEmail(request.email())){
            return ResponseEntity.badRequest().body(new AuthResponse(null, "niii", "Email déjà utilisé"));
        }
        Users user = Users.builder()
                .email(request.email())
                .nom(request.nom())
                .prenom(request.prenom())
                .mot_de_passe(passwordEncoder.encode(request.mot_de_passe()))
                .role(Role.valueOf(request.role().toUpperCase()))
                .build();
        userService.saveUsers(user);

        return ResponseEntity.ok(new AuthResponse(null, "Bearer", "Admin crée"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request){
        try{
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(request.email(),request.mot_de_passe());
            // Si l'authentification échoue, une AuthenticationException de Spring Security est lancée.
            authenticationManager.authenticate(token);

            Users user = userService.findByEmail(request.email()).orElseThrow();
            String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            return ResponseEntity.ok(new AuthResponse(jwt, "Bearer", "Connexion OK"));
        }catch (AuthenticationException e){ // <-- Capture l'exception de Spring Security
            // Vous pouvez loguer e.getMessage() ici pour plus de détails
            return ResponseEntity.status(401).body(new AuthResponse(null, "Bearer", "Identifiants invalides"));
        }
    }
}