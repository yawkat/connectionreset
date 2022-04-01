package com.example;

import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;

@Singleton
public class ResettingService {

    public Flux<byte[]> getSomeFlowable() {
        return Flux.just("<somexml>", "</somexml>").map(String::getBytes);
    }
}
