package com.example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import reactor.core.publisher.Flux;

@Controller("/v1")
public class ResettingEndpoint {

    private final ResettingService resettingService;

    public ResettingEndpoint(ResettingService resettingService) {
        this.resettingService = resettingService;
    }

    @Get("/stream")
    public Flux<byte[]> getStream() {
        return resettingService.getSomeFlowable();
    }
}
