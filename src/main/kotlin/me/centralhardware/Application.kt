package me.centralhardware

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.LinkedHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import me.centralhardware.db.DatabaseFactory
import me.centralhardware.plugins.configureRouting
import org.slf4j.LoggerFactory

fun main() {
    // Get port from environment variable or use default
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    // Initialize database
    try {
        DatabaseFactory.init()
        logger.info("Database initialized successfully")
    } catch (e: Exception) {
        logger.error("Failed to initialize database", e)
        throw e
    }

    // Create a serializers module to register LinkedHashMap serializer and for polymorphic serialization
    val mapSerializersModule = SerializersModule {
        // Register the custom serializer for LinkedHashMap
        contextual(LinkedHashMap::class) { _ ->
            me.centralhardware.serialization.AnyValueMapSerializer()
        }

        // Register LinkedHashMap for polymorphic serialization in the scope of Map
        polymorphic(Map::class) {
            subclass(LinkedHashMap::class as KClass<LinkedHashMap<String, Any?>>)
        }
    }

    // Install plugins
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            serializersModule = mapSerializersModule
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respondText(
                text = "500: ${cause.message}",
                status = HttpStatusCode.InternalServerError
            )
        }
    }

    // Configure routing
    configureRouting()

    logger.info("Application started")
}
