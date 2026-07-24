package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    // endpoint privado
    @GetMapping("/hola")
    public String hola() {

        return "hola";
    }
}
