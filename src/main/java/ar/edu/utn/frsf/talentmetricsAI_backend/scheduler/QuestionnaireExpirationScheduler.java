package ar.edu.utn.frsf.talentmetricsAI_backend.scheduler;

import ar.edu.utn.frsf.talentmetricsAI_backend.service.QuestionnaireService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QuestionnaireExpirationScheduler {

    private final QuestionnaireService questionnaireService;

    public QuestionnaireExpirationScheduler(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    /**
     * Se ejecuta todos los días a las 00:00 hs.
     * Cron expression: "Segundos Minutos Horas Día_del_mes Mes Día_de_la_semana"
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void procesarCuestionariosVencidos() {
        System.out.println("Iniciando job nocturno: finalización de cuestionarios vencidos...");
        questionnaireService.finalizarCuestionariosVencidos();
    }
}
