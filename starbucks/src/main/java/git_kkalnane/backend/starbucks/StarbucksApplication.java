package git_kkalnane.backend.starbucks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StarbucksApplication {

	public static void main(String[] args) {
		SpringApplication.run(StarbucksApplication.class, args);
	}

}
