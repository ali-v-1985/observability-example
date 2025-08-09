# Observability Example

A comprehensive Spring Boot 3 application demonstrating modern observability practices with the complete Grafana Labs stack using OpenTelemetry integration.

## Features

- **Spring Boot 3** with Java 21
- **OpenTelemetry** for instrumentation and data collection
- **Complete Grafana Labs Stack:**
  - **Grafana** - Visualization and dashboards
  - **Mimir** - Metrics storage (Prometheus-compatible)
  - **Loki** - Log aggregation
  - **Tempo** - Distributed tracing
  - **Pyroscope** - Continuous profiling
- **Grafana Alloy** - Unified observability data collection
- **Containerized** with Docker Compose

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Application   │───▶│  Grafana Alloy  │───▶│  Grafana Labs   │
│  (Spring Boot)  │    │   (Collector)   │    │     Stack       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │              ┌────────┴────────┐              │
         │              │                 │              │
    ┌────▼────┐    ┌────▼────┐       ┌────▼────┐    ┌────▼────┐
    │ Metrics │    │  Logs   │       │ Traces  │    │Profiles │
    │(Micrometer/│  │(Logback)│       │(OpenTel)│    │(Pyroscope)│
    │Prometheus) │  │         │       │         │    │         │
    └─────────┘    └─────────┘       └─────────┘    └─────────┘
         │              │                 │              │
         ▼              ▼                 ▼              ▼
    ┌─────────┐    ┌─────────┐       ┌─────────┐    ┌─────────┐
    │  Mimir  │    │  Loki   │       │  Tempo  │    │Pyroscope│
    └─────────┘    └─────────┘       └─────────┘    └─────────┘
         │              │                 │              │
         └──────────────┼─────────────────┼──────────────┘
                        │                 │
                        ▼                 ▼
                   ┌─────────────────────────┐
                   │       Grafana           │
                   │   (Visualization)       │
                   └─────────────────────────┘
```

## Quick Start

### Prerequisites

- Java 21
- Docker and Docker Compose
- Maven (or use the included Maven wrapper)

### Running the Application

1. **Start the observability stack:**
   ```bash
   docker-compose up -d
   ```

2. **Build and run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

   Or on Windows:
   ```cmd
   mvnw.cmd spring-boot:run
   ```

3. **Access the services:**
   - **Application**: http://localhost:8080
   - **Grafana**: http://localhost:3000 (admin/admin)
   - **Alloy**: http://localhost:12345
   - **Pyroscope**: http://localhost:4040

### Testing the Application

Make some requests to generate observability data:

```bash
curl "http://localhost:8080/hello?name=World"
curl "http://localhost:8080/hello?name=OpenTelemetry"
curl "http://localhost:8080/hello?name=Grafana"
```

## Observability Features

### Metrics
- **Custom metrics**: Request counters, response times
- **JVM metrics**: Memory, GC, threads
- **HTTP metrics**: Request/response statistics
- **OpenTelemetry metrics**: Via OTLP protocol

### Logs
- **Structured logging**: JSON format with logback
- **Correlation IDs**: Trace and span IDs in logs
- **Multiple log levels**: INFO, DEBUG, WARN, ERROR
- **Log aggregation**: Via Loki

### Traces
- **Distributed tracing**: OpenTelemetry instrumentation
- **Custom spans**: Manual instrumentation examples
- **Trace correlation**: Links to logs and metrics
- **Service maps**: Automatic topology discovery

### Profiling
- **Continuous profiling**: CPU and memory profiling
- **Integration**: Linked with traces and metrics
- **Flame graphs**: Performance visualization

## Configuration

### Application Configuration
Key configuration in `src/main/resources/application.yml`:

```yaml
# OpenTelemetry configuration
otel:
  service:
    name: observability-example
  exporter:
    otlp:
      endpoint: http://localhost:4318
  traces:
    exporter: otlp
  metrics:
    exporter: otlp,prometheus
```

### Alloy Configuration
Located in `observability/alloy/config.alloy`, handles:
- OTLP data reception
- Metrics scraping
- Log collection
- Data forwarding to backends

## Dashboards

Pre-configured Grafana dashboard includes:
- **Request Rate**: Real-time request metrics
- **Response Times**: Percentile-based latency metrics
- **Application Logs**: Searchable log viewer
- **Trace Search**: Distributed trace explorer
- **Service Map**: Application topology

## Development

### Adding Custom Metrics

```java
@Autowired
private MeterRegistry meterRegistry;

Counter customCounter = Counter.builder("custom_metric")
    .description("Description of metric")
    .register(meterRegistry);

customCounter.increment();
```

### Adding Custom Traces

```java
@Autowired
private Tracer tracer;

Span span = tracer.spanBuilder("custom-operation").startSpan();
try (Scope scope = span.makeCurrent()) {
    // Your code here
    span.setAttribute("custom.attribute", "value");
} finally {
    span.end();
}
```

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 3000, 3100, 3200, 4040, 8080, 9009, 12345 are available
2. **Docker resources**: Ensure sufficient memory (recommended 4GB+)
3. **Java version**: Application requires Java 21

### Checking Service Health

```bash
# Check all services
docker-compose ps

# Check application health
curl http://localhost:8080/actuator/health

# Check Grafana
curl http://localhost:3000/api/health
```

### Viewing Logs

```bash
# Application logs
docker-compose logs app

# All services logs
docker-compose logs

# Specific service logs
docker-compose logs grafana
docker-compose logs alloy
```

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.1**
- **OpenTelemetry 1.32.0**
- **Grafana 10.2.3**
- **Mimir 2.11.0**
- **Loki 2.9.4**
- **Tempo 2.3.1**
- **Pyroscope 1.4.0**
- **Alloy 1.0.0**

## License

This project is for educational purposes demonstrating observability best practices.
