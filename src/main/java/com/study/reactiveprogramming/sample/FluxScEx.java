package com.study.reactiveprogramming.sample;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class FluxScEx {
    public static void main(String[] args) throws InterruptedException {
//        Flux.range(1, 10)
//            .publishOn(Schedulers.newSingle("pub"))         //퍼블리싱을 하는 시점(onNext)부터 별도의 쓰레드 (consumer 느린경우)
//            .log()
//            .subscribeOn(Schedulers.newSingle("sub"))       //구독을 하는 시점(onSubscribe)부터 별도의 쓰레드 (producer가 느린경우)
//            .subscribe(System.out::println);
//
//        System.out.println("exit");

        // 별도의 쓰레드에서 실행 -> demon 쓰레드
        // JVM은 user 쓰레드가 없고 demon쓰레드만 남아있으면 강제로 종료됨.
        // 따라서 테스트하려면 main 쓰레드를 오래 살려놓기 위해 sleep
        Flux.interval(Duration.ofMillis(200))
            .take(10)       //원하는 데이터의 개수만큼만 받을 수 있음. (paging처리 처럼)
            .subscribe(s -> log.debug("onNext:{}", s));

        log.debug("exit");
        TimeUnit.SECONDS.sleep(5);
    }
}
