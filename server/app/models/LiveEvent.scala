package models

import play.api.libs.json.{Format, Json}

case class LiveEvent(
    name: String,
    place: String,
    date: String,
    description: String,
    url: String,
    image: String = "/assets/images/placeholder/318x180.png"
)

object LiveEvent {
  implicit val format: Format[LiveEvent] = Json.format
}

//TODO check this idea
case class NewLiveEvent(
    name: String,
    description: String,
    address: Address,
    location: Location,
    tags: Seq[String],
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
