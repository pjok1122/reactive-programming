package com.study.reactiveprogramming.sample.third;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class _08_01_ThreadPoolHell {
    /**
     * Linked-in에서 ThreadPool hell에 대해서 발표를 진행했었음.
     * 내용인 즉, 하나의 컴포넌트에서 다른 컴포넌트를 호출할 때, 계속 blocking 되어있으므로 ServletThread의 개수가 모자라지는 현상을 의미한다.
     * 이를 테스트 하기 위해 ServletThread 수를 1개로 제한하고, 외부 API를 호출하는 예제를 만들어 보자.
     */

    private RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/rest")
    public String rest(String idx) {
        String res = restTemplate.getForObject("http://localhost:8081/remote?req={req}", String.class, idx);
        return res;
    }

    AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();      //Spring 4.0
    @GetMapping("/async-rest")
    public ListenableFuture<ResponseEntity<String>> asyncRest(String idx) {
        return asyncRestTemplate.getForEntity("http://localhost:8081/remote?req={req}", String.class, idx);
    }

    private static AtomicInteger atomicInteger = new AtomicInteger(0);
    public static void main(String[] args) throws BrokenBarrierException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(100);
//        String url = "http://localhost:8080/rest?idx={idx}";
        String url = "http://localhost:8080/async-rest?idx={idx}";

        RestTemplate restTemplate = new RestTemplate();
        CyclicBarrier barrier = new CyclicBarrier(101); //쓰레드 동기화에 사용되는 기술.

        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                int idx = atomicInteger.getAndIncrement();

                barrier.await();        //101개의 await을 만날 때까지 여기서 대기하게 된다.
                log.info("Thread " + idx);

                StopWatch sw = new StopWatch();
                sw.start();
                String result = restTemplate.getForObject(url, String.class, idx);
                sw.stop();

                log.info("Elapsed: {} -> {}, result : {}" , idx, sw.getTotalTimeSeconds(), result);
                return null;
            });
        }

        StopWatch main = new StopWatch();
        main.start();
        barrier.await();

        executor.shutdown();
        executor.awaitTermination(200, TimeUnit.SECONDS);

        main.stop();
        log.info("Total : {}", main.getTotalTimeSeconds());


    }

}
