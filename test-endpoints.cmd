@echo off
echo Testing Observability Application Endpoints
echo ==========================================

echo Testing application endpoints...
curl -s "http://localhost:8080/hello?name=World"
echo.

curl -s "http://localhost:8080/hello?name=OpenTelemetry"
echo.

curl -s "http://localhost:8080/hello?name=Grafana"
echo.

echo Checking application health...
curl -s "http://localhost:8080/actuator/health"
echo.

echo Checking metrics endpoint...
curl -s "http://localhost:8080/actuator/prometheus" | findstr "hello_requests"
echo ... (showing hello metrics only)
echo.

echo Checking observability stack...

echo Grafana health:
curl -s "http://localhost:3000/api/health"
echo.

echo Alloy health:
curl -s "http://localhost:12345/-/healthy"
echo.

echo Pyroscope health:
curl -s "http://localhost:4040/api/v1/applications"
echo.

echo Test completed!
echo.
echo Access URLs:
echo - Application: http://localhost:8080/hello?name=YourName
echo - Grafana: http://localhost:3000 (admin/admin)
echo - Alloy: http://localhost:12345
echo - Pyroscope: http://localhost:4040
