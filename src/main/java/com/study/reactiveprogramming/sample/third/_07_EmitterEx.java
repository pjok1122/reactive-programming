package com.study.reactiveprogramming.sample.third;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class _07_EmitterEx {
    /**
     * Spring 4.2
     * ResponseBodyEmitter, SseEmitter, StreamingResponseBody
     * 데이터를 1개만 반환하는 것이 아니라, 데이터를 여러 번 보낼 수 있음.
     * 즉, 1번의 요청에 응답은 여러 번에 나누어서 전달하는 것과 같음.
     *
     * Spring에서 Emitter를 이용하면 Http의 복잡한 설정 없이 Streaming 형태로 데이터를 전달할 수 있음.
     */

    @GetMapping("/emitter")
    public ResponseBodyEmitter emitter() {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(() -> {
            for(int i=1; i<=50; i++) {
                try {
                    emitter.send("<p>Stream " + i + "</p>");
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return emitter;
    }
}
