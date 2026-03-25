package org.example.gestionhuilerieback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
		"org.example.gestionhuilerieback",
		"Controllers",
		"Services",
		"Mapper",
		"Config"
})
@EnableJpaRepositories(basePackages = {
		"Repositories"
})
@EntityScan(basePackages = {
		"Models"
})
public class GestionHuilerieBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionHuilerieBackApplication.class, args);
	}
}