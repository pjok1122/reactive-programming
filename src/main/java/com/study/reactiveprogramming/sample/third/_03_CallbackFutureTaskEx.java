package com.study.reactiveprogramming.sample.third;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.springframework.lang.NonNull;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class _03_CallbackFutureTaskEx {
    /**
     * 자바7까지는 callback을 처리하기 위해서는 이처럼 직접 정의해야 했음.
     * FutureTask는 작업이 끝나면 done()이 호출되므로, done()을 override하여 callback을 수행.
     * get()은 예외 발생 시 예외를 터뜨리지만, 이 작업을 done에서 처리하기보단 예외를 callback에서 수행하는게 더 우아한 방법.
     */
    interface SuccessCallback {
        void onSuccess(String result);
    }

    interface ExceptionCallback {
        void onError(Throwable t);
    }

    public static class CallbackFutureTask extends FutureTask<String> {
        SuccessCallback sc;
        ExceptionCallback ec;

        public CallbackFutureTask(Callable<String> callable, @NonNull SuccessCallback sc, @NonNull ExceptionCallback ec) {
            super(callable);
            this.sc = Objects.requireNonNull(sc);       //null이면 NPE 발생.
            this.ec = Objects.requireNonNull(ec);
        }

        @Override
        protected void done() {
            try {
                sc.onSuccess(get());
            } catch (InterruptedException e) {
                // 작업을 지금 하지말고 종료하라는 signal을 받았다는 의미이므로 Exception의 성격이 조금 다름.
                // 그냥 다시 interrupt를 걸어주면 됨.
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                // 비동기 작업 중 예외가 발생한 케이스
                ec.onError(e.getCause());   //e 자체는 wrapping class이므로 getCause를 꺼냄.
            }
        }
    }

    public static void main(String[] args) {
        // 비즈니스 코드와 기술적인 코드가 혼재 되어있다는 단점이 있음.

        ExecutorService es = Executors.newCachedThreadPool();
        //onSuccess
        CallbackFutureTask f = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        }, result -> log.info(result), e -> log.info("Error : " + e.getMessage()));

        es.execute(f);

        //onError
        CallbackFutureTask f2 = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            if (1 ==1) throw new RuntimeException("Async Error");
            return "Hello";
        }, result -> log.info(result), e -> log.info("Error : " + e.getMessage()));

        es.execute(f2);

    }
}
