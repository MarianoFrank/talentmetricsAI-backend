package ar.edu.utn.frsf.talentmetricsAI_backend.config;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Competency;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.CompetencyType;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Factor;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Option;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Question;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.QuestionType;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.CompetencyRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.FactorRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.QuestionRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CompetencyRepository competencyRepository;
    private final FactorRepository factorRepository;
    private final QuestionRepository questionRepository;

    public DataInitializer(CompetencyRepository competencyRepository, FactorRepository factorRepository,
            QuestionRepository questionRepository) {
        this.competencyRepository = competencyRepository;
        this.factorRepository = factorRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (competencyRepository.count() == 0) {

            // --- Competencias ---
            Competency backendComp = new Competency();
            backendComp.setCode("COMP-BACK-001");
            backendComp.setName("Backend Development");
            backendComp.setDescription("Server-side logic, database integration and architecture");
            backendComp.setType(CompetencyType.TECHNICAL);

            Competency frontendComp = new Competency();
            frontendComp.setCode("COMP-FRONT-001");
            frontendComp.setName("Frontend Development");
            frontendComp.setDescription("Client-side logic, UI/UX implementation and state management");
            frontendComp.setType(CompetencyType.TECHNICAL);

            competencyRepository.saveAll(List.of(backendComp, frontendComp));

            // --- Factores ---
            Factor springFactor = new Factor();
            springFactor.setCode("FACT-SPR-01");
            springFactor.setName("Java Spring Boot");
            springFactor.setDescription("REST APIs, JPA, and dependency injection");
            springFactor.setOrderNumber(1);
            springFactor.setCompetency(backendComp);

            Factor springSecurityFactor = new Factor();
            springSecurityFactor.setCode("FACT-SEC-02");
            springSecurityFactor.setName("Spring Security");
            springSecurityFactor.setDescription("Authentication, authorization and JWT");
            springSecurityFactor.setOrderNumber(2);
            springSecurityFactor.setCompetency(backendComp);

            Factor reactFactor = new Factor();
            reactFactor.setCode("FACT-REA-01");
            reactFactor.setName("React Ecosystem");
            reactFactor.setDescription("Component lifecycle, hooks, and PrimeReact UI");
            reactFactor.setOrderNumber(1);
            reactFactor.setCompetency(frontendComp);

            factorRepository.saveAll(List.of(springFactor, springSecurityFactor, reactFactor));

            System.out.println("Base data (Competencies and Factors) initialized successfully!");
            // --- PREGUNTAS Y OPCIONES ---
            // Usamos el método ayudante que está abajo de todo para armar las preguntas de
            // una

            System.out.println("Cargando preguntas de Spring Boot...");
            cargarPregunta(springFactor, "Excepción Lazy",
                    "¿Qué indica el error LazyInitializationException en Hibernate?",
                    "Evaluación sobre el ciclo de vida de la sesión en JPA",
                    QuestionType.SINGLE_CHOICE,
                    new String[] {
                            "Falta agregar la anotación @Table en la entidad.",
                            "Se intentó acceder a una relación perezosa pero la sesión de base de datos ya estaba cerrada.",
                            "La consulta JPQL tiene un error de sintaxis en el JOIN.",
                            "La base de datos se quedó sin conexiones en el pool."
                    },
                    new Integer[] { 0, 10, 0, 0 }); // 10 puntos a la correcta

            cargarPregunta(springFactor, "Inyección de Dependencias",
                    "¿Cuál es la forma más recomendada por la industria para inyectar dependencias en Spring?",
                    "Buenas prácticas de arquitectura en Spring Boot",
                    QuestionType.SINGLE_CHOICE,
                    new String[] {
                            "Usando @Autowired sobre los atributos (Field Injection).",
                            "Usando inyección por Constructor.",
                            "Instanciando los servicios manualmente con 'new'.",
                            "Usando inyección por métodos Setter exclusivamente."
                    },
                    new Integer[] { 2, 10, 0, 5 });

            cargarPregunta(springFactor, "Validación Global",
                    "¿Qué anotación se usa en una clase para capturar excepciones globalmente en toda la API?",
                    "Manejo de errores y respuestas estandarizadas",
                    QuestionType.SINGLE_CHOICE,
                    new String[] {
                            "@RestControllerAdvice",
                            "@GlobalErrorMapping",
                            "@ExceptionHandler",
                            "@ControllerErrorHandler"
                    },
                    new Integer[] { 10, 0, 3, 0 }); // 3 puntos a ExceptionHandler por ser parcialmente cierto

            System.out.println("Cargando preguntas de Spring Security...");
            cargarPregunta(springSecurityFactor, "Filtros de Seguridad",
                    "¿Cuál es el componente principal donde se configuran las reglas de acceso (ej: qué rutas son públicas) en las versiones modernas de Spring Security?",
                    "Arquitectura moderna de seguridad",
                    QuestionType.SINGLE_CHOICE,
                    new String[] {
                            "WebSecurityConfigurerAdapter",
                            "SecurityFilterChain",
                            "AuthenticationManagerBuilder",
                            "UserDetailsService"
                    },
                    new Integer[] { 0, 10, 0, 0 });

            cargarPregunta(springSecurityFactor, "Almacenamiento JWT",
                    "¿Dónde NO se recomienda guardar un token JWT en el cliente web por vulnerabilidades de tipo XSS?",
                    "Seguridad frontend-backend",
                    QuestionType.SINGLE_CHOICE,
                    new String[] {
                            "En memoria RAM de la aplicación.",
                            "En cookies con el flag HttpOnly.",
                            "En el LocalStorage del navegador.",
                            "En cookies seguras (Secure flag)."
                    },
                    new Integer[] { 0, 0, 10, 0 });

            System.out.println("Cargando preguntas de React...");
            cargarPregunta(reactFactor, "Hook useEffect",
                    "Si pasamos un array vacío [] como segundo parámetro al hook useEffect, ¿cuándo se ejecuta el efecto?",
                    "Ciclo de vida de componentes funcionales",
                    QuestionType.SINGLE_CHOICE,
                    new String[] {
                            "Se ejecuta en cada renderizado del componente.",
                            "Se ejecuta una sola vez al montar el componente.",
                            "Nunca se ejecuta.",
                            "Se ejecuta solo cuando el componente se desmonta."
                    },
                    new Integer[] { 0, 10, 0, 0 });

            cargarPregunta(reactFactor, "Estado Inmutable",
                    "¿Por qué no se debe modificar el estado de React directamente (ej: state.valor = 1)?",
                    "Manejo de estados e inmutabilidad",
                    QuestionType.SINGLE_CHOICE,
                    new String[] {
                            "Porque JavaScript no lo permite por defecto.",
                            "Porque React no se enteraría del cambio y no re-renderizaría la UI.",
                            "Porque tira un error de compilación.",
                            "Porque PrimeReact bloquea la variable."
                    },
                    new Integer[] { 0, 10, 0, 0 });

            System.out.println("¡Todas las preguntas de TalentMetrics AI fueron inyectadas como piña!");
        }
    }

    /**
     * Método ayudante (Helper) para ensuciar menos el método run().
     * Crea la pregunta, le asocia las opciones, y la guarda en la base de datos de
     * un plumazo.
     */
    private void cargarPregunta(Factor factor, String name, String text, String description, QuestionType type,
            String[] optionTexts, Integer[] optionWeights) {

        Question question = new Question();
        question.setFactor(factor);
        question.setName(name);
        question.setText(text);
        question.setDescription(description);
        question.setType(type);
        question.setVersion(1);

        // Recorremos los arrays de textos y pesos para armar las opciones
        for (int i = 0; i < optionTexts.length; i++) {
            Option option = new Option();
            option.setDisplayOrder(i + 1);
            option.setText(optionTexts[i]);
            option.setWeight(optionWeights[i]);

            // Relación bidireccional
            option.setQuestion(question);
            question.getOptions().add(option);
        }

        // Si tenés CascadeType.ALL o PERSIST en la lista de opciones de tu Entidad
        // Question,
        // al guardar la pregunta, Hibernate te guarda todas las opciones
        // automáticamente. ¡Un lujo!
        questionRepository.save(question);
    }
}
