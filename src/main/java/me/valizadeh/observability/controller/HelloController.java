package me.valizadeh.observability.controller;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);
    private final Counter helloCounter;
    private final ObservationRegistry observationRegistry;

    @Autowired
    public HelloController(MeterRegistry meterRegistry, ObservationRegistry observationRegistry) {
        this.helloCounter = Counter.builder("hello_requests_total")
                .description("Total number of hello requests")
                .register(meterRegistry);
        this.observationRegistry = observationRegistry;
    }

    @GetMapping("/hello")
    @Timed(value = "hello_request_duration", description = "Duration of hello requests")
    public HelloResponse hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return Observation.createNotStarted("hello-processing", observationRegistry)
                .lowCardinalityKeyValue("endpoint", "/hello")
                .highCardinalityKeyValue("user.name", name)
                .observe(() -> {
                    logger.info("Hello endpoint called with name: {}", name);
                    
                    // Increment custom metric
                    helloCounter.increment();
                    
                    // Simulate some processing time and CPU work for profiling
                    try {
                        long processingTime = 50 + (long) (Math.random() * 100);
                        simulateWork(processingTime);
                        Thread.sleep(processingTime / 2);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Thread interrupted during processing", e);
                    }
                    
                    String message = "Hello, " + name + "!";
                    logger.info("Returning response: {}", message);
                    
                    return new HelloResponse(message, name);
                });
    }

    /**
     * Simulate CPU-intensive work for profiling demonstration
     */
    private void simulateWork(long durationMs) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + durationMs;
        
        // Perform some CPU-intensive work
        while (System.currentTimeMillis() < endTime) {
            // Mathematical operations to consume CPU
            double result = 0;
            for (int i = 0; i < 1000; i++) {
                result += Math.sin(i) * Math.cos(i) * Math.sqrt(i + 1);
            }
            
            // Prevent optimization
            if (result < 0) {
                logger.debug("Computed result: {}", result);
            }
        }
    }

    public static class HelloResponse {
        private final String message;
        private final String name;

        public HelloResponse(String message, String name) {
            this.message = message;
            this.name = name;
        }

        public String getMessage() {
            return message;
        }

        public String getName() {
            return name;
        }
    }
}
