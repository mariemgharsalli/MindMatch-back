package esprit.mindmatch.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //allows all endpoints
                .allowedOrigins("http://localhost:4200") //allows the frontend to access the backend
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");// allowed methods
    }
}