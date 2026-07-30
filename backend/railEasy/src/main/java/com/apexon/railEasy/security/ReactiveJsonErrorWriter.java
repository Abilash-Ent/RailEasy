package com.apexon.railEasy.security;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Writes a small JSON error body directly to the reactive response.
 *
 * <p>Deliberately avoids depending on a Jackson {@code ObjectMapper} bean so it
 * works regardless of the Jackson version auto-configured by the framework.
 */
@Component
public class ReactiveJsonErrorWriter {

    public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String message, String detail) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String detailsJson = (detail == null || detail.isBlank())
                ? "null"
                : "[\"" + escape(detail) + "\"]";

        String body = "{"
                + "\"status\":" + status.value() + ","
                + "\"error\":\"" + escape(status.getReasonPhrase()) + "\","
                + "\"message\":\"" + escape(message) + "\","
                + "\"path\":\"" + escape(exchange.getRequest().getPath().value()) + "\","
                + "\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"details\":" + detailsJson
                + "}";

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

