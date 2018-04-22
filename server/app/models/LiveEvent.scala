package models

import java.time.LocalDate

import org.bson.types.ObjectId

case class LiveEvent(
    _id: ObjectId,
    name: String,
    startDate: LocalDate,
    endDate: LocalDate,
    description: String,
    webSite: String,
    imageUrl: String,
    address: Address,
    tags: Seq[String],
    speakers: Seq[String],
    languages: Seq[String],
    prices: Seq[Price],
    owner: String,
    status: String = LiveEvent.Added
)

object LiveEvent {
  val Added: String     = "Added"
  val Published: String = "Published"
}

case class Address(
    street: String,
    city: String,
    state: String,
    zip: String
)

case class Location(
    lat: Long,
    lon: Long
)

case class Price(
    name: String,
    description: Option[String],
    value: BigDecimal,
    startDate: LocalDate,
    endDate: LocalDate,
    priceType: String
)
