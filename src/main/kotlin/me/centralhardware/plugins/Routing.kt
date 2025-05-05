package me.centralhardware.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.centralhardware.db.DatabaseFactory
import me.centralhardware.models.HealthDataExport
import org.slf4j.LoggerFactory

fun Application.configureRouting() {
    val logger = LoggerFactory.getLogger("Routing")

    routing {
        get("/") {
            call.respondText("Health Auto Export Server is running!")
            logger.info("Root endpoint accessed")
        }

        // Health data routes
        route("/api/health") {
            post {
                try {
                    // User ID is no longer required for authorization
                    // Use a default value or get it from query parameter or header if provided
                    val userId = call.request.queryParameters["userId"] 
                        ?: call.request.headers["X-User-ID"] 
                        ?: "anonymous"

                    // Parse the health data export
                    val healthDataExport = call.receive<HealthDataExport>()
                    logger.info("Received health data export for user: $userId with ${healthDataExport.data.metrics.size} metrics and ${healthDataExport.data.workouts.size} workouts")

                    // Store data in ClickHouse
                    DatabaseFactory.storeHealthData(healthDataExport, userId)

                    call.respond(HttpStatusCode.Created, mapOf(
                        "status" to "success",
                        "metricsProcessed" to healthDataExport.data.metrics.size,
                        "workoutsProcessed" to healthDataExport.data.workouts.size
                    ))
                    logger.info("Health data stored successfully for user: $userId")
                } catch (e: Exception) {
                    logger.error("Error processing health data", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("status" to "error", "message" to (e.message ?: "Unknown error"))
                    )
                }
            }

        }
    }
}
