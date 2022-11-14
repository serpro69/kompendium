package io.bkbn.kompendium.playground

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.core.routes.redoc
import io.bkbn.kompendium.json.schema.KotlinXSchemaConfigurator
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.bkbn.kompendium.oas.serialization.KompendiumSerializersModule
import io.bkbn.kompendium.playground.util.Entry
import io.bkbn.kompendium.playground.util.ExampleResponse
import io.bkbn.kompendium.playground.util.ExceptionResponse
import io.bkbn.kompendium.playground.util.Response
import io.bkbn.kompendium.playground.util.Util.baseSpec
import io.bkbn.kompendium.playground.util.Version
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.time.Instant
import kotlin.reflect.typeOf
import kotlinx.serialization.json.Json

fun main() {
  embeddedServer(
    CIO,
    port = 8081,
    module = Application::mainModule
  ).start(wait = true)
}

private fun Application.mainModule() {
  install(ContentNegotiation) {
    json(Json {
      serializersModule = KompendiumSerializersModule.module
      encodeDefaults = true
      explicitNulls = false
    })
  }
  install(NotarizedApplication()) {
    spec = baseSpec
    // Adds support for @Transient and @SerialName
    // If you are not using them this is not required.
    schemaConfigurator = KotlinXSchemaConfigurator()

    customTypes = mapOf(
      // generic
      typeOf<Version>() to versionTypeDefinition,
      typeOf<Instant>() to instantTypeDefinition,
    )
  }
  routing {
    redoc(pageTitle = "Simple API Docs")

    route("/{id}") {
      idDocumentation()
      get {
        call.respond(HttpStatusCode.OK, ExampleResponse(true))
      }
      route("/profile") {
        profileDocumentation()
        get {
          call.respond(HttpStatusCode.OK, ExampleResponse(true))
        }
      }
    }
  }
}

private fun Route.idDocumentation() {
  install(NotarizedRoute()) {
    parameters = listOf(
      Parameter(
        name = "id",
        `in` = Parameter.Location.path,
        schema = TypeDefinition.STRING
      )
    )
    get = GetInfo.builder {
      summary("Get user by id")
      description("A very neat endpoint!")
      response {
        responseCode(HttpStatusCode.OK)
        responseType<ExampleResponse>()
        description("Will return whether or not the user is real 😱")
      }

      canRespond {
        responseType<ExceptionResponse>()
        responseCode(HttpStatusCode.NotFound)
        description("Indicates that a user with this id does not exist")
      }
    }
  }
}

private fun Route.profileDocumentation() {
  install(NotarizedRoute()) {
    parameters = listOf(
      Parameter(
        name = "id",
        `in` = Parameter.Location.path,
        schema = TypeDefinition.STRING
      )
    )
    get = GetInfo.builder {
      summary("Get a users profile")
      description("A cool endpoint!")
      response {
        responseCode(HttpStatusCode.OK)
        responseType<Response<Entry>>()
        description("Returns user profile information")
        examples(
          "test" to Response<Entry>(
            true,
            "test message",
            Entry(0, Version("1.2.3"), Instant.now())
          )
        )
      }
      canRespond {
        responseType<ExceptionResponse>()
        responseCode(HttpStatusCode.NotFound)
        description("Indicates that a user with this id does not exist")
      }
    }
  }
}

val versionTypeDefinition = TypeDefinition(
  type = "Version",
  format = "string",
  description = "Semantic version",
  default = "",
)

val instantTypeDefinition = TypeDefinition(
  type = "Instant",
  format = "string",
  description = "An instantaneous point on the time-line",
  default = "now()"
)
