#!/bin/bash

echo "Testing Observability Application Endpoints"
echo "=========================================="

# Application endpoints
echo "Testing application endpoints..."
curl -s "http://localhost:8080/hello?name=World" | jq '.' || echo "Response: $(curl -s "http://localhost:8080/hello?name=World")"
echo ""

curl -s "http://localhost:8080/hello?name=OpenTelemetry" | jq '.' || echo "Response: $(curl -s "http://localhost:8080/hello?name=OpenTelemetry")"
echo ""

curl -s "http://localhost:8080/hello?name=Grafana" | jq '.' || echo "Response: $(curl -s "http://localhost:8080/hello?name=Grafana")"
echo ""

# Health check
echo "Checking application health..."
curl -s "http://localhost:8080/actuator/health" | jq '.' || echo "Response: $(curl -s "http://localhost:8080/actuator/health")"
echo ""

# Metrics endpoint
echo "Checking metrics endpoint..."
curl -s "http://localhost:8080/actuator/prometheus" | head -10
echo "... (truncated metrics output)"
echo ""

# Observability stack health checks
echo "Checking observability stack..."

echo "Grafana health:"
curl -s "http://localhost:3000/api/health" | jq '.' || echo "Grafana not responding"
echo ""

echo "Alloy health:"
curl -s "http://localhost:12345/-/healthy" || echo "Alloy not responding"
echo ""

echo "Pyroscope health:"
curl -s "http://localhost:4040/api/v1/applications" | jq '.' || echo "Pyroscope not responding"
echo ""

echo "Test completed!"
echo ""
echo "Access URLs:"
echo "- Application: http://localhost:8080/hello?name=YourName"
echo "- Grafana: http://localhost:3000 (admin/admin)"
echo "- Alloy: http://localhost:12345"
echo "- Pyroscope: http://localhost:4040"
