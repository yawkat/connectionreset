package com.example;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static io.micronaut.http.HttpRequest.GET;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
public class ResettingEndpointRxClientTest {

    @Inject
    private ResettingService resettingService;

    @Inject
    @Client("/v1")
    private HttpClient rxHttpClient;

    @MockBean(ResettingService.class)
    ResettingService resettingService() {
        return mock(ResettingService.class);
    }

    @Test
    void client_shouldReadData() throws Exception {
        when(resettingService.getSomeFlowable()).thenReturn(Flux.just("<somexml>", "</somexml>").map(String::getBytes));


        Assertions.assertEquals(
                "<somexml></somexml>",
                Flux.from(rxHttpClient.exchange(GET("/stream"), byte[].class))
                        .map(HttpResponse::body)
                        .map(String::new)
                        .blockLast()
        );
    }


    @Test
    void client_shouldInform_whenConnectionReset() throws Exception {
        when(resettingService.getSomeFlowable()).thenReturn(Flux
                .just("<somexml>")
                .concatWith(
                        Flux.error(() -> new RuntimeException("Some error with xml construction"))
                )
                .concatWith(
                        Flux.just("</somexml>")
                )
                .map(String::getBytes)
        );

        Assertions.assertEquals(
                "<somexml>",
                Flux.from(rxHttpClient.exchange(GET("/stream"), byte[].class))
                        .map(HttpResponse::body)
                        .map(String::new)
                        .blockLast()
        );
    }
}
