package models

import java.time.ZonedDateTime

case class LiveEvent(
    name: String,
    description: String,
    webSite: String,
    image: String,
    address: Address,
    location: Location,
    tags: Set[String],
    speakers: Set[String],
    languages: Set[String],
    prices: Set[Price],
    rating: String
)

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
    start: ZonedDateTime,
    end: ZonedDateTime,
    priceType: String,
    days: Set[ZonedDateTime]
)
