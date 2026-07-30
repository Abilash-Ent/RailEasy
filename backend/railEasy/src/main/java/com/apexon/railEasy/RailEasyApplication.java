package com.apexon.railEasy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RailEasy — reactive train ticket reservation system.
 * Built with Spring WebFlux, Spring Data R2DBC (H2) and reactive JWT security.
 *
 * <p>This is a pure R2DBC application. Database schema/seed initialization is
 * handled reactively by {@code R2dbcInitializerConfig} against the R2DBC
 * ConnectionFactory, and Boot's own SQL init is disabled via
 * {@code spring.sql.init.mode=never}, so no JDBC DataSource is involved.
 */
@SpringBootApplication
public class RailEasyApplication {

	public static void main(String[] args) {
		SpringApplication.run(RailEasyApplication.class, args);
	}

}
