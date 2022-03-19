package com.study.reactiveprogramming.sample.third;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class _06_DeferredResultQueueEx {
    /**
     * Worker thread 수를 늘리지 않고 ServletThread를 반환하는 다른 방법에 대해서 살펴보자.
     * DeferredResult Queue는 스프링 3.2부터 등장한 기술.
     *
     * 이 기술은 별도의 worker thread가 생성되는 것이 아니고, 객체만 생성해서 반환해두면 됨.
     * 따라서 servlet thread를 최소한으로 사용할 수 있다는 장점이 있음.
     * 이 객체를 받은 클라이언트는 응답을 대기하는 상태가 되고, 어디선가 해당 객체에 setResult를 하게 되면 클라이언트에게 응답이 전달된다.
     *
     * 즉, 아래 예시에서는 '/dr'을 호출했을 때는 대기상태에 빠지고, '/dr/event'가 호출되면 비로소 응답이 되는 것을 볼 수 있음.
     *
     * ex) 사용예시 (적절한 방법은 아님)
     * 채팅방에 30명이 들어가있을 때, DeferredResult로 대기상태에 있다가 event가 발생하면 30명에게 메시지를 전달.
     * 메시지를 전달하면 또 DeferredResult로 대기상태에 진입.
     *
     * ex) 사용예시
     * 이메일이나 어떤 결과를 다른 서비스로부터 받았을 때 그 결과를 알려주어야 할 때 사용할 수도 있음.
     *
     */
    Queue<DeferredResult<String>> results = new ConcurrentLinkedDeque<>();

    @GetMapping("/dr")
    public DeferredResult<String> deferredResult() {
        log.info("deferredResult");
        DeferredResult<String> dr = new DeferredResult<>(60*1000L);
        results.add(dr);
        return dr;
    }

    @GetMapping("/dr/count")
    public String drCount() {
        return String.valueOf(results.size());
    }

    @GetMapping("/dr/event")
    public String drEvent(String msg) {
        for (DeferredResult<String> dr : results) {
            dr.setResult("Hello " + msg);
            results.remove(dr);
        }
        return "OK";
    }

    static AtomicInteger idx = new AtomicInteger(0);
    public static void main(String[] args) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(100);
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/dr";
        StopWatch main = new StopWatch();
        main.start();

        for (int i = 0; i < 100; i++) {
            es.execute(() -> {
                int index = idx.getAndIncrement();
                log.info("Thread {}", index);

                StopWatch sw = new StopWatch();
                sw.start();
                restTemplate.getForObject(url, String.class);
                sw.stop();

                log.info("Elapsed: {} -> {} " , index, sw.getTotalTimeSeconds());
            });
        }

        es.shutdown();      //graceful한 shutdown이므로 shutdown 명령이 호출된다고 바로 종료되는 것이 아님.
        es.awaitTermination(100, TimeUnit.SECONDS); // 100초간 대기.(blocking) 작업이 완료됐으면 무시하고 종료됨.
        main.stop();
        log.info("Total : {}", main.getTotalTimeSeconds());
    }
}
