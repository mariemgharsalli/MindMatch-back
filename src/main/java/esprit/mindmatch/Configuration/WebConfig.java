package esprit.mindmatch.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.core.StreamWriteConstraints;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // allows all endpoints
                .allowedOrigins("http://localhost:4200") // allows the frontend to access the backend
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*"); // allowed methods
    }

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .dateFormat(new StdDateFormat())
                .modulesToInstall(new JavaTimeModule())
                .postConfigurer(objectMapper -> {
                    // Augmenter la limite de profondeur d'imbrication
                    objectMapper.getFactory().setStreamWriteConstraints(
                            StreamWriteConstraints.builder()
                                    .maxNestingDepth(2000) // Augmentez selon vos besoins
                                    .build()
                    );
                });
    }
}