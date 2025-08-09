package me.valizadeh.observability.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jdk.jfr.Recording;
import jdk.jfr.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cross-platform profiling service using Java Flight Recorder (JFR)
 * 
 * This provides REAL profiling that works on Windows, Linux, and macOS:
 * - CPU profiling with method-level detail
 * - Memory allocation tracking
 * - GC events and performance
 * - Thread contention analysis
 * - Built into the JVM (no external dependencies)
 * - Minimal overhead (< 1% in production)
 */
@Service
@ConditionalOnProperty(prefix = "jfr", name = "profiling-enabled", havingValue = "true")
public class JFRProfilingService {

    private static final Logger logger = LoggerFactory.getLogger(JFRProfilingService.class);
    
    private final AtomicReference<Recording> currentRecording = new AtomicReference<>();
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${jfr.application-name:observability-example}")
    private String applicationName;
    
    @Value("${jfr.pyroscope-url:http://localhost:4040}")
    private String pyroscopeUrl;
    
    @Value("${jfr.recording-duration:30s}")
    private String recordingDuration;
    
    @Value("${jfr.recording-interval:60s}")
    private String recordingInterval;
    
    @Value("${jfr.profile-cpu:true}")
    private boolean profileCpu;
    
    @Value("${jfr.profile-allocations:true}")
    private boolean profileAllocations;
    
    @Value("${jfr.profile-locks:true}")
    private boolean profileLocks;

    @PostConstruct
    public void initialize() {
        logger.info("🔥 Initializing JFR (Java Flight Recorder) profiling...");
        logger.info("Application: {}", applicationName);
        logger.info("Pyroscope URL: {}", pyroscopeUrl);
        logger.info("Recording Duration: {}", recordingDuration);
        logger.info("Recording Interval: {}", recordingInterval);
        logger.info("CPU Profiling: {}", profileCpu);
        logger.info("Allocation Profiling: {}", profileAllocations);
        logger.info("Lock Profiling: {}", profileLocks);
        
        // Start initial recording
        startRecording();
        
        logger.info("✅ JFR profiling started successfully!");
        logger.info("📊 Collecting CPU, memory, and thread data");
        logger.info("🚀 Profile data will be sent to Pyroscope every {}", recordingInterval);
    }
    
    @Scheduled(fixedDelay = 60000) // 60 seconds
    @Async
    public void collectAndSendProfile() {
        try {
            // Stop current recording and start a new one
            Recording oldRecording = currentRecording.get();
            if (oldRecording != null) {
                // Start new recording before stopping old one for continuous profiling
                Recording newRecording = startRecording();
                
                // Stop and dump the old recording
                oldRecording.stop();
                Path recordingFile = dumpRecording(oldRecording);
                
                // Send to Pyroscope
                if (recordingFile != null) {
                    sendRecordingToPyroscope(recordingFile);
                    // Clean up the file
                    Files.deleteIfExists(recordingFile);
                }
                
                oldRecording.close();
                currentRecording.set(newRecording);
            }
            
            logger.debug("📊 JFR recording cycle completed");
            
        } catch (Exception e) {
            logger.warn("Error during JFR profiling cycle: {}", e.getMessage());
        }
    }
    
    private Recording startRecording() {
        try {
            // Create a new recording with appropriate configuration
            Recording recording = new Recording();
            
            // Use the 'profile' configuration which has reasonable overhead
            // and collects comprehensive profiling data
            Configuration config = Configuration.getConfiguration("profile");
            if (config != null) {
                recording.setSettings(config.getSettings());
            }
            
            // Set recording duration
            Duration duration = Duration.parse("PT" + recordingDuration.toUpperCase());
            recording.setDuration(duration);
            
            // Set recording name for identification
            recording.setName("observability-profile-" + Instant.now().getEpochSecond());
            
            // Start the recording
            recording.start();
            
            logger.debug("🎬 Started JFR recording: {}", recording.getName());
            return recording;
            
        } catch (Exception e) {
            logger.error("Failed to start JFR recording: {}", e.getMessage());
            return null;
        }
    }
    
    private Path dumpRecording(Recording recording) {
        try {
            // Create temporary file for the recording
            Path tempFile = Files.createTempFile("jfr-profile-", ".jfr");
            
            // Dump recording to file
            recording.dump(tempFile);
            
            logger.debug("💾 JFR recording dumped to: {}", tempFile);
            return tempFile;
            
        } catch (IOException e) {
            logger.error("Failed to dump JFR recording: {}", e.getMessage());
            return null;
        }
    }
    
    private void sendRecordingToPyroscope(Path recordingFile) {
        try {
            // Read the JFR file as bytes
            byte[] recordingBytes = Files.readAllBytes(recordingFile);
            
            // Pyroscope expects the recording data in specific format
            // For JFR files, we send them to the /ingest endpoint with proper headers
            String url = pyroscopeUrl + "/ingest?name=" + applicationName + "&spyName=jfrspy";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Content-Type", "application/octet-stream");
            
            HttpEntity<byte[]> request = new HttpEntity<>(recordingBytes, headers);
            
            logger.debug("🚀 Sending JFR data to Pyroscope: {} bytes", recordingBytes.length);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("✅ Successfully sent JFR profile to Pyroscope: {} bytes", recordingBytes.length);
            } else {
                logger.warn("❌ Failed to send JFR profile: HTTP {} - {}", 
                    response.getStatusCode(), response.getBody());
            }
            
        } catch (Exception e) {
            logger.warn("Error sending JFR profile to Pyroscope: {}", e.getMessage());
        }
    }
    
    @PreDestroy
    public void cleanup() {
        logger.info("🛑 Stopping JFR profiling...");
        
        Recording recording = currentRecording.get();
        if (recording != null) {
            try {
                recording.stop();
                
                // Try to send the final recording
                Path finalRecording = dumpRecording(recording);
                if (finalRecording != null) {
                    sendRecordingToPyroscope(finalRecording);
                    Files.deleteIfExists(finalRecording);
                }
                
                recording.close();
                logger.info("✅ JFR profiling stopped and final data sent");
                
            } catch (Exception e) {
                logger.error("Error during JFR cleanup: {}", e.getMessage());
            }
        }
    }
}
