package com.example;

import io.reactivex.Flowable;

import javax.inject.Singleton;

@Singleton
public class ResettingService {

    public Flowable<byte[]> getSomeFlowable() {
        return Flowable.just("<somexml>", "</somexml>").map(String::getBytes);
    }
}
