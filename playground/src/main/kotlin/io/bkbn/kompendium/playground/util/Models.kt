package io.bkbn.kompendium.playground.util

import io.ktor.server.locations.Location
import kotlinx.datetime.Instant
import java.time.Instant as JavaInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder

@Serializable
data class ExampleResponse(val isReal: Boolean)

@Serializable
data class CustomTypeResponse(
  val thing: String,
  val timestamp: Instant
)

@Serializable
data class ExceptionResponse(val message: String)

@Location("/list/{name}/page/{page}")
data class Listing(val name: String, val page: Int)

@Location("/type/{name}") data class Type(val name: String) {
  // In these classes we have to include the `name` property matching the parent.
  @Location("/edit") data class Edit(val parent: Type)
  @Location("/other/{page}") data class Other(val parent: Type, val page: Int)
}

@Serializable
data class Response<T : Any>(
  val success: Boolean,
  val additionalMessage: String = "",
  val data: T
)

@Serializable
data class Entry(
  val id: Int,
  @Serializable(with = VersionSerializer::class) val version: Version,
  @Serializable(with = JavaInstantSerializer::class) val timestamp: JavaInstant,
)

object VersionSerializer : KSerializer<Version> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Version", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Version) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): Version = Version(decoder.decodeString())
}

object JavaInstantSerializer: KSerializer<JavaInstant> {

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JavaInstant", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): JavaInstant = JavaInstant.parse(decoder.decodeString())

  override fun serialize(encoder: Encoder, value: JavaInstant) = encoder.encodeString(value.toString())
}

@JvmInline
@Serializable(with = VersionSerializer::class)
value class Version(private val value: String)
