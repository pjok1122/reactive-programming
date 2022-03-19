package com.study.reactiveprogramming.sample.third;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.springframework.lang.NonNull;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class _02_FutureTaskEx {

    public static void main(String[] args) {
        /**
         * FutureTask는 미래에 수행해야 할 Runnable(Callable) 인터페이스를 생성자 파라미터로 받음.
         * FutureTask는 Runnable을 구현하고 있음.
         * 익명클래스로 만들어서 완료 후 동작을 재정의할 수 있으나, callback의 동작은 아님.
         */
        ExecutorService es = Executors.newCachedThreadPool();

        FutureTask<String> ft = new FutureTask<>(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        }) {
            @SneakyThrows
            @Override
            protected void done() {
                log.info(get());
            }
        };

        es.execute(ft);
        es.shutdown();
    }
}
