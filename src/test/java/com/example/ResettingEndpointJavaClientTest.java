package com.example;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Flowable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
public class ResettingEndpointJavaClientTest {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Inject
    private ResettingService resettingService;
    @Inject
    private EmbeddedServer embeddedServer;


    @MockBean(ResettingService.class)
    ResettingService resettingService() {
        return mock(ResettingService.class);
    }

    @Test
    void client_shouldReadData() throws Exception {
        when(resettingService.getSomeFlowable()).thenReturn(Flowable.just("<somexml>", "</somexml>").map(String::getBytes));

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .GET()
                        .uri(embeddedServer.getURI().resolve("/v1/stream"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals("<somexml></somexml>", response.body());
    }


    @Test
    void client_shouldInform_whenConnectionReset() throws Exception {
        when(resettingService.getSomeFlowable()).thenReturn(Flowable
                .just("<somexml>")
                .concatWith(
                        Flowable.error(() -> new RuntimeException("Some error with xml construction"))
                )
                .concatWith(
                        Flowable.just("</somexml>")
                )
                .map(String::getBytes)
        );

        IOException exception = assertThrows(IOException.class, () -> httpClient.send(
                HttpRequest.newBuilder()
                        .GET()
                        .uri(embeddedServer.getURI().resolve("/v1/stream"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        ));

        // Maybe not the best message but nested cause has info about EOF
        assertEquals("chunked transfer encoding, state: READING_LENGTH", exception.getMessage());
        assertEquals("EOF reached while reading", exception.getCause().getCause().getMessage());
    }
}
