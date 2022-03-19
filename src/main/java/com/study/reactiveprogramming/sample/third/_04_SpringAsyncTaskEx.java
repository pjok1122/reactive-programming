package com.study.reactiveprogramming.sample.third;

import java.util.concurrent.Future;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
@RequiredArgsConstructor
public class _04_SpringAsyncTaskEx implements ApplicationRunner {
    /**
     * Spring에서는 Async annotation을 이용하면 기술적인 코드와 비즈니스 코드의 분리가 가능해짐.
     * 반환타입을 Future<T>로 만들어서 사용할 수 있음.
     * 고전적인 Async 결과 확인 방법
     * 1. 보통 Async 작업은 작업의 결과를 DB에 저장하고, 작업이 끝났는지는 별도의 API call로 체크함.
     * 2. 두번째 방법은 HttpSession에 Future를 저장하고, Future의 isDone을 수행하며 체크할 수 있음.
     *
     * Spring 4.0에서는 ListenableFuture<T> 라는 클래스를 지원. ListenableFuture는 callback을 지원하므로, 작업이 종료되었을 때 별도의 로직을 등록해두고, 신경쓰지 않아도 됨.
     */
    private final MyService service;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("run()");
        ListenableFuture<String> f = service.hello();//시간이 많이 걸리는 작업.
        f.addCallback(s -> log.info("callback" + s), e -> log.info(e.getMessage()));
        log.info("exit" + f.isDone());
    }

    @Service
    public static class MyService {
        @Async
        public ListenableFuture<String> hello() throws InterruptedException {
            log.info("hello");
            Thread.sleep(1000);
            return new AsyncResult<>("Hello");
        }
    }

}
