package com.apexon.railEasy.config;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

import java.sql.SQLException;

/**
 * Starts an embedded H2 TCP server and Web console for development so the
 * in-memory R2DBC database can be inspected via a browser.
 *
 * <p>The web console is available at {@code http://localhost:8082}. Connect with
 * JDBC URL: {@code jdbc:h2:tcp://localhost:9092/mem:raileasy} (user {@code sa}, no password).
 * These fixed-port servers are dev-only: they are disabled for the {@code prod}
 * and {@code test} profiles.
 */
@Slf4j
@Configuration
@Profile("!prod & !test")
public class H2ServerConfig {

    /** Exposes the JVM-local in-memory databases over TCP for the console. */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer() throws SQLException {
        log.info("Starting H2 TCP server on port 9092");
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092", "-ifNotExists");
    }

    /** The browser-based H2 console. */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @DependsOn("h2TcpServer")
    public Server h2WebConsole() throws SQLException {
        log.info("Starting H2 web console on http://localhost:8082");
        return Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082");
    }
}

