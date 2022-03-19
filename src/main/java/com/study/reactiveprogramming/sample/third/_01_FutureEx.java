package com.study.reactiveprogramming.sample.third;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class _01_FutureEx {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        /*
         Future: 가장 기본이 되는 인터페이스
         비동기 작업을 수행한 결과(js의 promise)

         execute는 Runnable 인터페이스를 받으므로 리턴이 불가.
         submit을 이용하면 리턴이 가능. (Callable 인터페이스를 사용하기 때문. Callable은 예외도 밖에서 처리.
         결과는 Future라는 객체로 받아올 수 있음. 하지만 futere.get() 메서드는 작업이 완료될 때까지 blocking되는 blocking method임.
         결과를 반환할 때까지 다른 비즈니스 로직을 수행할 수 있고, future.isDone()으로 확인해가며 처리도 가능.

         */
        ExecutorService es = Executors.newCachedThreadPool();

        Future<String> f = es.submit(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        });

        // 다른 비즈니스 로직
        log.info("Another business logic");

        log.info(f.get());  //blocking
        log.info("Exit");
    }
}
