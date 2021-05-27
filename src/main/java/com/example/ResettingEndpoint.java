package com.example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.reactivex.Flowable;

@Controller("/v1")
public class ResettingEndpoint {

    private final ResettingService resettingService;

    public ResettingEndpoint(ResettingService resettingService) {
        this.resettingService = resettingService;
    }

    @Get("/stream")
    public Flowable<byte[]> getStream() {
        return resettingService.getSomeFlowable();
    }
}
