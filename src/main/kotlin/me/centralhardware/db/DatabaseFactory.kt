package me.centralhardware.db

import com.clickhouse.jdbc.ClickHouseDataSource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.centralhardware.models.*
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Properties
import java.util.UUID

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)
    private lateinit var dataSource: ClickHouseDataSource

    fun init() {
        try {
            val properties = Properties()
            val url = System.getenv("CLICKHOUSE_URL") ?: "jdbc:clickhouse://localhost:8123/health_data"
            val user = System.getenv("CLICKHOUSE_USER") ?: "default"
            val password = System.getenv("CLICKHOUSE_PASSWORD") ?: ""

            properties.setProperty("user", user)
            properties.setProperty("password", password)

            dataSource = ClickHouseDataSource(url, properties)

            // Create tables if they don't exist
            createTables()

            logger.info("ClickHouse connection initialized successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize ClickHouse connection", e)
            throw e
        }
    }

    private fun createTables() {
        connection().use { connection ->
            // Create database if not exists
            connection.createStatement().use { statement ->
                statement.execute("CREATE DATABASE IF NOT EXISTS health_data")
            }

            // Create common health metrics table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.common_metrics (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        metric_name String,
                        units String,
                        qty Float64,
                        timestamp DateTime64(3),
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, metric_name, timestamp)
                """.trimIndent())
            }

            // Create blood pressure table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.blood_pressure (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        systolic Float64,
                        diastolic Float64,
                        timestamp DateTime64(3),
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, timestamp)
                """.trimIndent())
            }

            // Create heart rate table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.heart_rate (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        min_rate Float64,
                        avg_rate Float64,
                        max_rate Float64,
                        timestamp DateTime64(3),
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, timestamp)
                """.trimIndent())
            }

            // Create sleep analysis table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.sleep_analysis (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        date Date,
                        asleep Float64,
                        sleep_start DateTime64(3),
                        sleep_end DateTime64(3),
                        sleep_source String,
                        in_bed Float64,
                        in_bed_start DateTime64(3),
                        in_bed_end DateTime64(3),
                        in_bed_source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, date)
                """.trimIndent())
            }

            // Create blood glucose table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.blood_glucose (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        qty Float64,
                        meal_time String,
                        timestamp DateTime64(3),
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, timestamp)
                """.trimIndent())
            }

            // Create sexual activity table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.sexual_activity (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        unspecified Float64,
                        protection_used Float64,
                        protection_not_used Float64,
                        timestamp DateTime64(3),
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, timestamp)
                """.trimIndent())
            }

            // Create handwashing table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.handwashing (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        qty Float64,
                        value String,
                        timestamp DateTime64(3),
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, timestamp)
                """.trimIndent())
            }

            // Create toothbrushing table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.toothbrushing (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        qty Float64,
                        value String,
                        timestamp DateTime64(3),
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, timestamp)
                """.trimIndent())
            }

            // Create insulin delivery table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.insulin_delivery (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        qty Float64,
                        reason String,
                        timestamp DateTime64(3),
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, timestamp)
                """.trimIndent())
            }

            // Create heart rate notifications table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.heart_rate_notifications (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        start_time DateTime64(3),
                        end_time DateTime64(3),
                        threshold Float64 NULL,
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, start_time)
                """.trimIndent())
            }

            // Create heart rate notification details table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.heart_rate_notification_details (
                        id UUID DEFAULT generateUUIDv4(),
                        notification_id UUID,
                        hr Float64,
                        hrv Float64 NULL,
                        start_time DateTime64(3),
                        end_time DateTime64(3),
                        duration Float64,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (notification_id, start_time)
                """.trimIndent())
            }

            // Create symptoms table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.symptoms (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        symptom_name String,
                        severity String,
                        start_time DateTime64(3),
                        end_time DateTime64(3),
                        user_entered UInt8,
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, symptom_name, start_time)
                """.trimIndent())
            }

            // Create state of mind table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.state_of_mind (
                        id String,
                        user_id String,
                        start_time DateTime64(3),
                        end_time DateTime64(3),
                        kind String,
                        labels Array(String),
                        associations Array(String),
                        valence Float64,
                        valence_classification Int32,
                        metadata String, -- JSON string
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, start_time)
                """.trimIndent())
            }

            // Create ECG table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.ecg (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        start_time DateTime64(3),
                        end_time DateTime64(3),
                        classification String,
                        severity String,
                        average_heart_rate Float64,
                        number_of_voltage_measurements Int32,
                        sampling_frequency Float64,
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, start_time)
                """.trimIndent())
            }

            // Create ECG voltage measurements table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.ecg_voltage_measurements (
                        id UUID DEFAULT generateUUIDv4(),
                        ecg_id UUID,
                        timestamp DateTime64(3),
                        voltage Float64,
                        units String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (ecg_id, timestamp)
                """.trimIndent())
            }

            // Create workouts table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.workouts (
                        id UUID DEFAULT generateUUIDv4(),
                        user_id String,
                        name String,
                        start_time DateTime64(3),
                        end_time DateTime64(3),
                        total_energy Float64 NULL,
                        total_energy_unit String NULL,
                        active_energy Float64 NULL,
                        active_energy_unit String NULL,
                        max_heart_rate Float64 NULL,
                        avg_heart_rate Float64 NULL,
                        step_count Float64 NULL,
                        step_cadence Float64 NULL,
                        total_swimming_stroke_count Float64 NULL,
                        swim_cadence Float64 NULL,
                        distance Float64 NULL,
                        distance_unit String NULL,
                        speed Float64 NULL,
                        speed_unit String NULL,
                        flights_climbed Float64 NULL,
                        intensity Float64 NULL,
                        temperature Float64 NULL,
                        temperature_unit String NULL,
                        humidity Float64 NULL,
                        elevation_ascent Float64 NULL,
                        elevation_descent Float64 NULL,
                        elevation_unit String NULL,
                        source String,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (user_id, start_time)
                """.trimIndent())
            }

            // Create workout heart rate data table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.workout_heart_rate (
                        id UUID DEFAULT generateUUIDv4(),
                        workout_id UUID,
                        timestamp DateTime64(3),
                        qty Float64,
                        is_recovery UInt8,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (workout_id, timestamp)
                """.trimIndent())
            }

            // Create workout route data table
            connection.createStatement().use { statement ->
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS health_data.workout_route (
                        id UUID DEFAULT generateUUIDv4(),
                        workout_id UUID,
                        timestamp DateTime64(3),
                        latitude Float64,
                        longitude Float64,
                        altitude Float64,
                        insert_time DateTime64(3) DEFAULT now64(3)
                    ) ENGINE = MergeTree()
                    ORDER BY (workout_id, timestamp)
                """.trimIndent())
            }
        }
    }

    fun storeHealthData(healthDataExport: HealthDataExport, userId: String) {
        connection().use { connection ->
            // Store metrics
            healthDataExport.data.metrics.forEach { metric ->
                when (metric) {
                    is CommonHealthMetric -> storeCommonHealthMetric(connection, metric, userId)
                    is BloodPressureMetric -> storeBloodPressureMetric(connection, metric, userId)
                    is HeartRateMetric -> storeHeartRateMetric(connection, metric, userId)
                    is SleepAnalysisMetric -> storeSleepAnalysisMetric(connection, metric, userId)
                    is BloodGlucoseMetric -> storeBloodGlucoseMetric(connection, metric, userId)
                    is SexualActivityMetric -> storeSexualActivityMetric(connection, metric, userId)
                    is HandwashingMetric -> storeHandwashingMetric(connection, metric, userId)
                    is ToothbrushingMetric -> storeToothbrushingMetric(connection, metric, userId)
                    is InsulinDeliveryMetric -> storeInsulinDeliveryMetric(connection, metric, userId)
                    is HeartRateNotificationsMetric -> storeHeartRateNotificationsMetric(connection, metric, userId)
                    is SymptomsMetric -> storeSymptomsMetric(connection, metric, userId)
                    is StateOfMindMetric -> storeStateOfMindMetric(connection, metric, userId)
                    is ECGMetric -> storeECGMetric(connection, metric, userId)
                }
            }

            // Store workouts
            healthDataExport.data.workouts.forEach { workout ->
                storeWorkout(connection, workout, userId)
            }
        }
    }

    private fun storeCommonHealthMetric(connection: Connection, metric: CommonHealthMetric, userId: String) {
        val sql = """
            INSERT INTO health_data.common_metrics 
            (user_id, metric_name, units, qty, timestamp, source)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            metric.data.forEach { data ->
                statement.setString(1, userId)
                statement.setString(2, metric.name)
                statement.setString(3, metric.units)
                statement.setDouble(4, data.qty)
                statement.setTimestamp(5, parseTimestamp(data.date))
                statement.setString(6, "Health Auto Export")

                statement.executeUpdate()
            }
        }
    }

    private fun storeBloodPressureMetric(connection: Connection, metric: BloodPressureMetric, userId: String) {
        val sql = """
            INSERT INTO health_data.blood_pressure 
            (user_id, systolic, diastolic, timestamp, source)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            metric.data.forEach { data ->
                statement.setString(1, userId)
                statement.setDouble(2, data.systolic)
                statement.setDouble(3, data.diastolic)
                statement.setTimestamp(4, parseTimestamp(data.date))
                statement.setString(5, "Health Auto Export")

                statement.executeUpdate()
            }
        }
    }

    private fun storeHeartRateMetric(connection: Connection, metric: HeartRateMetric, userId: String) {
        val sql = """
            INSERT INTO health_data.heart_rate 
            (user_id, min_rate, avg_rate, max_rate, timestamp, source)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            metric.data.forEach { data ->
                statement.setString(1, userId)
                statement.setDouble(2, data.Min)
                statement.setDouble(3, data.Avg)
                statement.setDouble(4, data.Max)
                statement.setTimestamp(5, parseTimestamp(data.date))
                statement.setString(6, "Health Auto Export")

                statement.executeUpdate()
            }
        }
    }

    private fun storeSleepAnalysisMetric(connection: Connection, metric: SleepAnalysisMetric, userId: String) {
        val sql = """
            INSERT INTO health_data.sleep_analysis 
            (user_id, date, asleep, sleep_start, sleep_end, sleep_source, in_bed, in_bed_start, in_bed_end, in_bed_source)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            metric.data.forEach { data ->
                statement.setString(1, userId)
                statement.setDate(2, java.sql.Date.valueOf(data.date.substring(0, 10)))
                statement.setDouble(3, data.asleep)
                statement.setTimestamp(4, parseTimestamp(data.sleepStart))
                statement.setTimestamp(5, parseTimestamp(data.sleepEnd))
                statement.setString(6, data.sleepSource)
                statement.setDouble(7, data.inBed)
                statement.setTimestamp(8, parseTimestamp(data.inBedStart))
                statement.setTimestamp(9, parseTimestamp(data.inBedEnd))
                statement.setString(10, data.inBedSource)

                statement.executeUpdate()
            }
        }
    }

    private fun storeBloodGlucoseMetric(connection: Connection, metric: BloodGlucoseMetric, userId: String) {
        val sql = """
            INSERT INTO health_data.blood_glucose 
            (user_id, qty, meal_time, timestamp, source)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            metric.data.forEach { data ->
                statement.setString(1, userId)
                statement.setDouble(2, data.qty)
                statement.setString(3, data.mealTime)
                statement.setTimestamp(4, parseTimestamp(data.date))
                statement.setString(5, "Health Auto Export")

                statement.executeUpdate()
            }
        }
    }

    private fun storeSexualActivityMetric(connection: Connection, metric: SexualActivityMetric, userId: String) {
        val sql = """
            INSERT INTO health_data.sexual_activity 
            (user_id, unspecified, protection_used, protection_not_used, timestamp, source)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            metric.data.forEach { data ->
                statement.setString(1, userId)
                statement.setDouble(2, data.Unspecified)
                statement.setDouble(3, data.`Protection Used`)
                statement.setDouble(4, data.`Protection Not Used`)
                statement.setTimestamp(5, parseTimestamp(data.date))
                statement.setString(6, "Health Auto Export")

                statement.executeUpdate()
            }
        }
    }

    private fun storeHandwashingMetric(connection: Connection, metric: HandwashingMetric, userId: String) {
        val sql = """
            INSERT INTO health_data.handwashing 
            (user_id, qty, value, timestamp, source)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            metric.data.forEach { data ->
                statement.setString(1, userId)
                statement.setDouble(2, data.qty)
                statement.setString(3, data.value)
                statement.setTimestamp(4, parseTimestamp(data.date))
                statement.setString(5, "Health Auto Export")

                statement.executeUpdate()
            }
        }
    }

    private fun storeToothbrushingMetric(connection: Connection, metric: ToothbrushingMetric, userId: String) {
        val sql = """
            INSERT INTO health_data.toothbrushing 
            (user_id, qty, value, timestamp, source)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            metric.data.forEach { data ->
                statement.setString(1, userId)
                statement.setDouble(2, data.qty)
                statement.setString(3, data.value)
                statement.setTimestamp(4, parseTimestamp(data.date))
                statement.setString(5, "Health Auto Export")

                statement.executeUpdate()
            }
        }
    }

    private fun storeInsulinDeliveryMetric(connection: Connection, metric: InsulinDeliveryMetric, userId: String) {
        val sql = """
            INSERT INTO health_data.insulin_delivery 
            (user_id, qty, reason, timestamp, source)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            metric.data.forEach { data ->
                statement.setString(1, userId)
                statement.setDouble(2, data.qty)
                statement.setString(3, data.reason)
                statement.setTimestamp(4, parseTimestamp(data.date))
                statement.setString(5, "Health Auto Export")

                statement.executeUpdate()
            }
        }
    }

    private fun storeHeartRateNotificationsMetric(connection: Connection, metric: HeartRateNotificationsMetric, userId: String) {
        metric.data.forEach { notification ->
            // Insert notification
            val notificationId = UUID.randomUUID()
            val notificationSql = """
                INSERT INTO health_data.heart_rate_notifications 
                (id, user_id, start_time, end_time, threshold, source)
                VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()

            connection.prepareStatement(notificationSql).use { statement ->
                statement.setObject(1, notificationId)
                statement.setString(2, userId)
                statement.setTimestamp(3, parseTimestamp(notification.start))
                statement.setTimestamp(4, parseTimestamp(notification.end))
                statement.setObject(5, notification.threshold)
                statement.setString(6, "Health Auto Export")

                statement.executeUpdate()
            }

            // Insert heart rate data
            notification.heartRate?.forEach { hrData ->
                val hrSql = """
                    INSERT INTO health_data.heart_rate_notification_details 
                    (notification_id, hr, hrv, start_time, end_time, duration)
                    VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent()

                connection.prepareStatement(hrSql).use { statement ->
                    statement.setObject(1, notificationId)
                    statement.setDouble(2, hrData.hr)
                    statement.setNull(3, java.sql.Types.DOUBLE)
                    statement.setTimestamp(4, parseTimestamp(hrData.timestamp.start))
                    statement.setTimestamp(5, parseTimestamp(hrData.timestamp.end))
                    statement.setDouble(6, hrData.timestamp.interval.duration)

                    statement.executeUpdate()
                }
            }

            // Insert heart rate variation data
            notification.heartRateVariation?.forEach { hrvData ->
                val hrvSql = """
                    INSERT INTO health_data.heart_rate_notification_details 
                    (notification_id, hr, hrv, start_time, end_time, duration)
                    VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent()

                connection.prepareStatement(hrvSql).use { statement ->
                    statement.setObject(1, notificationId)
                    statement.setNull(2, java.sql.Types.DOUBLE)
                    statement.setDouble(3, hrvData.hrv)
                    statement.setTimestamp(4, parseTimestamp(hrvData.timestamp.start))
                    statement.setTimestamp(5, parseTimestamp(hrvData.timestamp.end))
                    statement.setDouble(6, hrvData.timestamp.interval.duration)

                    statement.executeUpdate()
                }
            }
        }
    }

    private fun storeSymptomsMetric(connection: Connection, metric: SymptomsMetric, userId: String) {
        val sql = """
            INSERT INTO health_data.symptoms 
            (user_id, symptom_name, severity, start_time, end_time, user_entered, source)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            metric.data.forEach { data ->
                statement.setString(1, userId)
                statement.setString(2, data.name)
                statement.setString(3, data.severity)
                statement.setTimestamp(4, parseTimestamp(data.start))
                statement.setTimestamp(5, parseTimestamp(data.end))
                statement.setInt(6, if (data.userEntered) 1 else 0)
                statement.setString(7, data.source)

                statement.executeUpdate()
            }
        }
    }

    private fun storeStateOfMindMetric(connection: Connection, metric: StateOfMindMetric, userId: String) {
        val sql = """
            INSERT INTO health_data.state_of_mind 
            (id, user_id, start_time, end_time, kind, labels, associations, valence, valence_classification, metadata)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            metric.data.forEach { data ->
                statement.setString(1, data.id)
                statement.setString(2, userId)
                statement.setTimestamp(3, parseTimestamp(data.start))
                statement.setTimestamp(4, parseTimestamp(data.end))
                statement.setString(5, data.kind)
                statement.setArray(6, connection.createArrayOf("String", data.labels.toTypedArray()))
                statement.setArray(7, connection.createArrayOf("String", data.associations.toTypedArray()))
                statement.setDouble(8, data.valence)
                statement.setInt(9, data.valenceClassification)
                statement.setString(10, Json.encodeToString(data.metadata))

                statement.executeUpdate()
            }
        }
    }

    private fun storeECGMetric(connection: Connection, metric: ECGMetric, userId: String) {
        metric.data.forEach { ecgData ->
            // Insert ECG record
            val ecgId = UUID.randomUUID()
            val ecgSql = """
                INSERT INTO health_data.ecg 
                (id, user_id, start_time, end_time, classification, severity, average_heart_rate, number_of_voltage_measurements, sampling_frequency, source)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            connection.prepareStatement(ecgSql).use { statement ->
                statement.setObject(1, ecgId)
                statement.setString(2, userId)
                statement.setTimestamp(3, parseTimestamp(ecgData.start))
                statement.setTimestamp(4, parseTimestamp(ecgData.end))
                statement.setString(5, ecgData.classification)
                statement.setString(6, ecgData.severity)
                statement.setDouble(7, ecgData.averageHeartRate)
                statement.setInt(8, ecgData.numberOfVoltageMeasurements)
                statement.setDouble(9, ecgData.samplingFrequency)
                statement.setString(10, ecgData.source)

                statement.executeUpdate()
            }

            // Insert voltage measurements
            val voltageSql = """
                INSERT INTO health_data.ecg_voltage_measurements 
                (ecg_id, timestamp, voltage, units)
                VALUES (?, ?, ?, ?)
            """.trimIndent()

            connection.prepareStatement(voltageSql).use { statement ->
                ecgData.voltageMeasurements.forEach { voltage ->
                    statement.setObject(1, ecgId)
                    statement.setTimestamp(2, parseTimestamp(voltage.date))
                    statement.setDouble(3, voltage.voltage)
                    statement.setString(4, voltage.units)

                    statement.executeUpdate()
                }
            }
        }
    }

    private fun storeWorkout(connection: Connection, workout: Workout, userId: String) {
        // Insert workout record
        val workoutId = UUID.randomUUID()
        val workoutSql = """
            INSERT INTO health_data.workouts 
            (id, user_id, name, start_time, end_time, 
             total_energy, total_energy_unit, active_energy, active_energy_unit,
             max_heart_rate, avg_heart_rate, step_count, step_cadence,
             total_swimming_stroke_count, swim_cadence, distance, distance_unit,
             speed, speed_unit, flights_climbed, intensity, temperature, temperature_unit,
             humidity, elevation_ascent, elevation_descent, elevation_unit, source)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(workoutSql).use { statement ->
            statement.setObject(1, workoutId)
            statement.setString(2, userId)
            statement.setString(3, workout.name)
            statement.setTimestamp(4, parseTimestamp(workout.start))
            statement.setTimestamp(5, parseTimestamp(workout.end))

            // Optional fields
            if (workout.totalEnergy != null) {
                statement.setDouble(6, workout.totalEnergy.qty)
                statement.setString(7, workout.totalEnergy.units)
            } else {
                statement.setNull(6, java.sql.Types.DOUBLE)
                statement.setNull(7, java.sql.Types.VARCHAR)
            }

            if (workout.activeEnergy != null) {
                statement.setDouble(8, workout.activeEnergy.qty)
                statement.setString(9, workout.activeEnergy.units)
            } else {
                statement.setNull(8, java.sql.Types.DOUBLE)
                statement.setNull(9, java.sql.Types.VARCHAR)
            }

            statement.setObject(10, workout.maxHeartRate?.qty)
            statement.setObject(11, workout.avgHeartRate?.qty)
            statement.setObject(12, workout.stepCount?.qty)
            statement.setObject(13, workout.stepCadence?.qty)
            statement.setObject(14, workout.totalSwimmingStrokeCount?.qty)
            statement.setObject(15, workout.swimCadence?.qty)

            if (workout.distance != null) {
                statement.setDouble(16, workout.distance.qty)
                statement.setString(17, workout.distance.units)
            } else {
                statement.setNull(16, java.sql.Types.DOUBLE)
                statement.setNull(17, java.sql.Types.VARCHAR)
            }

            if (workout.speed != null) {
                statement.setDouble(18, workout.speed.qty)
                statement.setString(19, workout.speed.units)
            } else {
                statement.setNull(18, java.sql.Types.DOUBLE)
                statement.setNull(19, java.sql.Types.VARCHAR)
            }

            statement.setObject(20, workout.flightsClimbed?.qty)
            statement.setObject(21, workout.intensity?.qty)

            if (workout.temperature != null) {
                statement.setDouble(22, workout.temperature.qty)
                statement.setString(23, workout.temperature.units)
            } else {
                statement.setNull(22, java.sql.Types.DOUBLE)
                statement.setNull(23, java.sql.Types.VARCHAR)
            }

            statement.setObject(24, workout.humidity?.qty)

            if (workout.elevation != null) {
                statement.setDouble(25, workout.elevation.ascent)
                statement.setDouble(26, workout.elevation.descent)
                statement.setString(27, workout.elevation.units)
            } else {
                statement.setNull(25, java.sql.Types.DOUBLE)
                statement.setNull(26, java.sql.Types.DOUBLE)
                statement.setNull(27, java.sql.Types.VARCHAR)
            }

            statement.setString(28, "Health Auto Export")

            statement.executeUpdate()
        }

        // Insert heart rate data
        if (workout.heartRateData != null) {
            val hrSql = """
                INSERT INTO health_data.workout_heart_rate 
                (workout_id, timestamp, qty, is_recovery)
                VALUES (?, ?, ?, ?)
            """.trimIndent()

            connection.prepareStatement(hrSql).use { statement ->
                workout.heartRateData.forEach { hr ->
                    statement.setObject(1, workoutId)
                    statement.setTimestamp(2, parseTimestamp(hr.date))
                    statement.setDouble(3, hr.qty)
                    statement.setInt(4, 0) // Not recovery
                    statement.executeUpdate()
                }
            }
        }

        // Insert heart rate recovery data
        if (workout.heartRateRecovery != null) {
            val hrRecoverySql = """
                INSERT INTO health_data.workout_heart_rate 
                (workout_id, timestamp, qty, is_recovery)
                VALUES (?, ?, ?, ?)
            """.trimIndent()

            connection.prepareStatement(hrRecoverySql).use { statement ->
                workout.heartRateRecovery.forEach { hr ->
                    statement.setObject(1, workoutId)
                    statement.setTimestamp(2, parseTimestamp(hr.date))
                    statement.setDouble(3, hr.qty)
                    statement.setInt(4, 1) // Is recovery
                    statement.executeUpdate()
                }
            }
        }

        // Insert route data
        if (workout.route != null) {
            val routeSql = """
                INSERT INTO health_data.workout_route 
                (workout_id, timestamp, latitude, longitude, altitude)
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent()

            connection.prepareStatement(routeSql).use { statement ->
                workout.route.forEach { point ->
                    statement.setObject(1, workoutId)
                    statement.setTimestamp(2, parseTimestamp(point.timestamp))
                    statement.setDouble(3, point.lat)
                    statement.setDouble(4, point.lon)
                    statement.setDouble(5, point.altitude)
                    statement.executeUpdate()
                }
            }
        }
    }

    private fun parseTimestamp(dateStr: String): Timestamp {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
        return Timestamp(format.parse(dateStr).time)
    }

    private fun connection(): Connection {
        return dataSource.connection
    }
}
