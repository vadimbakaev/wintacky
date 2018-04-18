package repositories.codec

import java.time._

import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.{BsonReader, BsonWriter}

class LocalDateCodec extends Codec[LocalDate] {

  override def encode(writer: BsonWriter, value: LocalDate, encoderContext: EncoderContext): Unit =
    writer.writeDateTime(value.atStartOfDay(ZoneId.systemDefault()).toInstant.toEpochMilli)

  override def getEncoderClass: Class[LocalDate] = classOf[LocalDate]

  override def decode(reader: BsonReader, decoderContext: DecoderContext): LocalDate =
    Instant.ofEpochMilli(reader.readDateTime()).atZone(ZoneId.systemDefault()).toLocalDate
}
