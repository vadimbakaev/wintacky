package models

import play.api.libs.json.{Format, Json}

case class LiveEventStub(
    name: String,
    place: String,
    date: String,
    description: String,
    url: String,
    image: String = "/assets/images/placeholder/318x180.png"
)

object LiveEventStub {
  implicit val format: Format[LiveEventStub] = Json.format
}
