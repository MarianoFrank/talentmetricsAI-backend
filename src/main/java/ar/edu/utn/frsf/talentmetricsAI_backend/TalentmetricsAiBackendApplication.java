package ar.edu.utn.frsf.talentmetricsAI_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity // Habilita la seguridad a nivel de método, permitiendo el uso de anotaciones
                      // como @PreAuthorize y @PostAuthorize para controlar el acceso a métodos
                      // específicos en los controladores y servicios.
@EnableAsync // Habilita la ejecución de métodos de forma asíncrona, permitiendo que ciertos
             // métodos se ejecuten en hilos separados y no bloqueen el hilo principal.
@EnableScheduling // Habilita la programación de tareas, permitiendo la ejecución de métodos en
                  // intervalos
                  // regulares o en momentos específicos, útil para tareas programadas.
public class TalentmetricsAiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalentmetricsAiBackendApplication.class, args);
    }

}
