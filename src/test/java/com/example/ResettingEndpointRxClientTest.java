package com.example;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Flowable;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static io.micronaut.http.HttpRequest.GET;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
public class ResettingEndpointRxClientTest {

    @Inject
    private ResettingService resettingService;

    @Inject
    @Client("/v1")
    private RxHttpClient rxHttpClient;

    @MockBean(ResettingService.class)
    ResettingService resettingService() {
        return mock(ResettingService.class);
    }

    @Test
    void client_shouldReadData() throws Exception {
        when(resettingService.getSomeFlowable()).thenReturn(Flowable.just("<somexml>", "</somexml>").map(String::getBytes));

        rxHttpClient.exchange(GET("/stream"), byte[].class)
                .map(HttpResponse::body)
                .map(String::new)
                .test()
                .await()
                .assertNoErrors()
                .assertValue("<somexml></somexml>"::equals);
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

        rxHttpClient.exchange(GET("/stream"), byte[].class)
                .map(HttpResponse::body)
                .map(String::new)
                .test()
                .await()
                .assertNoErrors() // There should be some error!
                .assertValue("<somexml>"::equals);
    }
}
