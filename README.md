# Health Auto Export Server

A Kotlin-based server application that receives health data from [health-auto-export](https://github.com/Lybron/health-auto-export) and stores it in ClickHouse database.

## Features

- RESTful API for receiving health data in the format exported by health-auto-export
- Support for all health metrics and workout data types
- ClickHouse database integration for efficient storage and querying
- Built with Ktor framework for high performance

## Prerequisites

- JDK 22
- ClickHouse database server
- Gradle 8.5 or higher

## Configuration

The application can be configured using environment variables or the default values in the configuration files:

### Environment Variables

- `PORT`: The port on which the server will run (default: 8080)
- `CLICKHOUSE_URL`: The URL of the ClickHouse database (default: jdbc:clickhouse://localhost:8123/health_data)
- `CLICKHOUSE_USER`: The username for ClickHouse (default: default)
- `CLICKHOUSE_PASSWORD`: The password for ClickHouse (default: empty)

## API Endpoints

### Root Endpoint

```
GET /
```

Returns a simple message indicating that the server is running.

### Health Data Endpoints

```
POST /api/health
```

Receives health data from the health-auto-export client and stores it in ClickHouse.

Required: User ID must be provided either as a query parameter (`?userId=user123`) or as a header (`X-User-ID: user123`).

Request body should contain the JSON data exported by health-auto-export, following the format described in the [health-auto-export documentation](https://github.com/Lybron/health-auto-export).

Example response:
```json
{
  "status": "success",
  "metricsProcessed": 5,
  "workoutsProcessed": 1
}
```


## Supported Health Data Types

The server supports all health data types exported by health-auto-export, including:

### Common Health Metrics
- Steps, Distance, Flights Climbed, etc.

### Special Format Health Metrics
- Blood Pressure (systolic/diastolic)
- Heart Rate (min/avg/max)
- Sleep Analysis
- Blood Glucose
- Sexual Activity
- Handwashing
- Toothbrushing
- Insulin Delivery
- Heart Rate Notifications
- Symptoms
- State of Mind
- ECG

### Workouts
- All workout types with associated data (heart rate, route, energy, etc.)

## Building and Running

### Building the Application

```bash
./gradlew build
```

### Running the Application

```bash
./gradlew run
```

Or with custom configuration:

```bash
PORT=9000 CLICKHOUSE_URL=jdbc:clickhouse://myserver:8123/health_data ./gradlew run
```

### Using Docker

The application can be run using Docker and Docker Compose, which will set up both the application and ClickHouse database.

#### Prerequisites

- Docker

#### Building the Docker Image

If you want to build the Docker image separately:

```bash
docker build -t health-auto-export-server .
```

#### Running the Docker Container

If you want to run the Docker container separately (requires a running ClickHouse instance):

```bash
docker run -p 8080:8080 \
  -e CLICKHOUSE_URL=jdbc:clickhouse://your-clickhouse-host:8123/health_data \
  -e CLICKHOUSE_USER=default \
  -e CLICKHOUSE_PASSWORD= \
  health-auto-export-server
```

## Database Schema

The application creates multiple tables in ClickHouse to store different types of health data:

- `common_metrics`: For common health metrics (steps, distance, etc.)
- `blood_pressure`: For blood pressure measurements
- `heart_rate`: For heart rate measurements
- `sleep_analysis`: For sleep analysis data
- `blood_glucose`: For blood glucose measurements
- `sexual_activity`: For sexual activity data
- `handwashing`: For handwashing data
- `toothbrushing`: For toothbrushing data
- `insulin_delivery`: For insulin delivery data
- `heart_rate_notifications`: For heart rate notifications
- `symptoms`: For symptoms data
- `state_of_mind`: For state of mind data
- `ecg`: For ECG data
- `workouts`: For workout data
- `workout_heart_rate`: For workout heart rate data
- `workout_route`: For workout route data

Each table is optimized for the specific data type it stores, with appropriate columns and indexes.

## Testing

The application includes unit tests for the API endpoints. To run the tests:

```bash
./gradlew test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
