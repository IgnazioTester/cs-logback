package ch.qos.logback.classic.service;

public interface EventMergerService {
    void mergeEvents() throws InterruptedException;
}
