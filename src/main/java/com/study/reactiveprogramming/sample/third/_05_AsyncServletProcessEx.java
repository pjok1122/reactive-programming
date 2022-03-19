package com.study.reactiveprogramming.sample.third;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class _05_AsyncServletProcessEx {
    /**
     * blocking I/O는 기본적으로 context switching이 일어나므로 CPU 자원을 많이 사용하게 됨.
     * Servlet은 기본적으로 Inputstream을 사용하기 때문에 blocking I/O
     *
     * Tomcat (쓰레드풀 5개) - default 200개
     * 요청1      ServletThread1  -> [ req -> blocking I/O (DB, API) -> res ]
     * 요청2      ServletThread2
     * 요청3      ServletThread3
     * 요청4      ServletThread4
     * 요청5      ServletThread5
     *
     * 큐에서 나머지 요청들 대기중. queue = [요청6, 요청7, 요청8 ...]
     *
     * 이 상황에서 DB에서 조회하는 blocking I/O가 있기 때문에 서비스의 지연이 발생하는 것.
     * CPU를 많이 쓰는 것도 아닌데, blocking I/O 때문에 레이턴시는 높아지고, 처리율은 낮아짐.
     *
     * 쓰레드를 많이 늘리면 어떨까?
     * - Thread 하나 당 약 1MB이므로 메모리 사용량이 증가함.
     * - Thread의 개수가 많아질 수록 Context switching이 많이 발생하므로 CPU 사용량이 증가함.
     * -> 레이턴시와 처리율이 떨어짐.
     *
     * Servlet 3.0에서 비동기 Servlet이라는 것은, ServletThread1이 비동기 작업 단계에 들어갔을 때, 별도의 요청을 받아서 처리할 수 있게 함.
     * 비동기 작업 작업이 끝나는 즉시 Servlet Thread를 재할당하는 구조.
     * Servlet 3.1에서 Callback이 등장하여, Servlet thread가 비동기 작업이 끝나면 어떤 동작을 해야 하는지 callback 처리가 가능해짐.
     *
     * 비동기 서블릿의 비동기 작업 처리 과정
     *
     * Client의 요청 -> NIO Connector -> ServletThread 할당 -> Worker Thread 할당 -> ServletThread 즉시 반납
     * Worker Thread 작업 완료 -> Servlet Thread 재할당 -> Response 생성 -> NIO Connector -> Client
     */

    @GetMapping("/async")
    public String async() throws InterruptedException {
        log.info("async");
        Thread.sleep(2000);     //시간이 오래걸리는 작업
        return "hello";
    }

    /**
     * Spring에 Callbale<>을 리턴하면 Spring이 알아서 쓰레드를 할당하여 일을 시킴.
     */
    @GetMapping("/callable")
    public Callable<String> callable() throws InterruptedException {
        log.info("callable");
        return () -> {
            log.info("async worker thread");
            Thread.sleep(2000);     //시간이 오래걸리는 작업
            return "hello";
        };
    }

    /**
     * 성능테스트
     *
     * 동시에 100개의 요청이 들어올 때, async와 callable의 응답속도는 현저히 차이가 난다.
     * server.tomcat.threads.max=20
     * spring.task.execution.pool.core-size=100
     *
     * callable의 경우, servlet thread의 수를 1개만 사용하더라도 굉장한 응답속도를 보이는데, 이게 의미가 있을까?
     * 결국은 worker thread가 늘어나기 때문에 큰 효용은 없음.
     * 다만, 작업 시간이 오래걸리는 경우, worker thread로 돌렸을 때, servlet thread를 재사용할 수 있다는 정도만 인지하자.
     *
     * Worker thread를 늘리지 않고 사용자 응답성과 처리율을 높일 순 없을까?
     */
    static AtomicInteger idx = new AtomicInteger(0);
    public static void main(String[] args) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(100);
        RestTemplate restTemplate = new RestTemplate();
//        String url = "http://localhost:8080/async";
        String url = "http://localhost:8080/callable";
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
