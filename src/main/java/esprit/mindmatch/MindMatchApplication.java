package esprit.mindmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class MindMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MindMatchApplication.class, args);
    }

}
