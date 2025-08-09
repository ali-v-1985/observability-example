package me.valizadeh.observability.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Pyroscope continuous profiling configuration
 * 
 * REAL PROFILING APPROACH (commented out due to Windows compatibility issues):
 * This shows the proper way to integrate Pyroscope for continuous profiling
 * using the async-profiler Java agent.
 */
@Configuration
public class PyroscopeConfig {

    private static final Logger logger = LoggerFactory.getLogger(PyroscopeConfig.class);

    @Value("${pyroscope.application-name:observability-example}")
    private String applicationName;

    @Value("${pyroscope.server-address:http://localhost:4040}")
    private String serverAddress;

    @Value("${pyroscope.profiling-enabled:false}")
    private boolean profilingEnabled;

    @PostConstruct
    public void initializePyroscope() {
        if (!profilingEnabled) {
            logger.info("🚫 Profiling is disabled (set pyroscope.profiling-enabled=true to enable)");
            logger.info("ℹ️  Note: Pyroscope agent currently has Windows compatibility issues");
            return;
        }

        logger.info("🔥 Initializing Pyroscope continuous profiling...");
        logger.info("Application: {}", applicationName);
        logger.info("Pyroscope Server: {}", serverAddress);
        
        /* 
         * PROPER PROFILING IMPLEMENTATION (commented out due to Windows issues):
         * 
         * Required dependency in pom.xml:
         * <dependency>
         *     <groupId>io.pyroscope</groupId>
         *     <artifactId>agent</artifactId>
         *     <version>0.13.0</version>
         * </dependency>
         * 
         * Required imports:
         * import io.pyroscope.javaagent.PyroscopeAgent;
         * import io.pyroscope.javaagent.config.Config;
         * 
         * Configuration in application.yml:
         * pyroscope:
         *   application-name: ${spring.application.name}
         *   server-address: http://localhost:4040
         *   profiling-enabled: true
         *   profiling-interval: 10s
         *   # async-profiler configuration
         *   alloc: 2m
         *   cpu: true
         *   wall: true
         * 
         * Code to start the agent:
         * Config config = new Config.Builder()
         *     .setApplicationName(applicationName)
         *     .setServerAddress(serverAddress)
         *     .build();
         * 
         * PyroscopeAgent.start(config);
         * 
         * This would provide REAL continuous profiling with:
         * - Statistical sampling at high frequency (100Hz)
         * - Call stack sampling during CPU work
         * - Flame graph generation showing method timing
         * - Memory allocation profiling
         * - Wall clock time profiling
         * - Integration with async-profiler for maximum accuracy
         * - Automatic collection without manual thread inspection
         */
        
        logger.warn("⚠️  Profiling agent not started - Windows compatibility issues");
        logger.info("💡 For real profiling, use Linux/macOS environment with async-profiler");
        logger.info("🔧 Alternative: Use JFR (Java Flight Recorder) + manual upload to Pyroscope");
    }
}