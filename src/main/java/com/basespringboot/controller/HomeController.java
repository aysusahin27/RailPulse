package com.basespringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur racine — sert index.html pour la SPA RailPulse.
 * Couvre "/" et toute route sans extension (ex: /dashboard, /trains)
 * afin d'éviter une erreur Whitelabel 404 lors du rechargement de page.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    /**
     * Fallback SPA : routes sans extension de fichier
     * (ex : /trains, /profile) → index.html.
     * Les fichiers statiques (*.css, *.js, *.png...) ne sont PAS capturés
     * grâce au regex [^\\.]+
     */
    @GetMapping("/{path:[^\\.]+}")
    public String spa() {
        return "forward:/index.html";
    }
}
