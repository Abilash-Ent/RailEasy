package com.apexon.railEasy.exception;

import com.apexon.railEasy.dto.response.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private ServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/trains/99"));
    }

    @Test
    void handleNotFound_returns404WithMessage() {
        ResponseEntity<ApiError> response =
                handler.handleNotFound(new ResourceNotFoundException("Not here"), exchange());

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Not here");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/trains/99");
    }

    @Test
    void handleDuplicate_returns409() {
        ResponseEntity<ApiError> response =
                handler.handleDuplicate(new DuplicateResourceException("Exists"), exchange());

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).isEqualTo("Exists");
    }

    @Test
    void handleBusiness_returns400() {
        ResponseEntity<ApiError> response =
                handler.handleBusiness(new BusinessValidationException("Bad"), exchange());

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Bad");
    }

    @Test
    void handleInvalidCredentials_returns401() {
        ResponseEntity<ApiError> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException("Nope"), exchange());

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody().getMessage()).isEqualTo("Nope");
    }

    @Test
    void handleValidation_returns400WithFieldDetails() {
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(new FieldError("obj", "email", "Email is required")));

        ResponseEntity<ApiError> response = handler.handleValidation(ex, exchange());

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getDetails()).containsExactly("email: Email is required");
    }

    @Test
    void handleResponseStatus_usesReasonWhenPresent() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "Denied");

        ResponseEntity<ApiError> response = handler.handleResponseStatus(ex, exchange());

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody().getMessage()).isEqualTo("Denied");
    }

    @Test
    void handleResponseStatus_fallsBackToReasonPhraseWhenReasonNull() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);

        ResponseEntity<ApiError> response = handler.handleResponseStatus(ex, exchange());

        assertThat(response.getStatusCode().value()).isEqualTo(501);
        assertThat(response.getBody().getMessage()).isEqualTo(HttpStatus.NOT_IMPLEMENTED.getReasonPhrase());
    }

    @Test
    void handleGeneric_returns500WithSafeMessage() {
        ResponseEntity<ApiError> response =
                handler.handleGeneric(new RuntimeException("boom"), exchange());

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }
}

