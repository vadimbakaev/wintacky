package models

import java.time.LocalDate

import play.api.libs.json.{Format, Json}

case class LiveEventView(
    id: String,
    name: String,
    startDate: LocalDate,
    endDate: LocalDate,
    description: String,
    webSite: String,
    imageUrl: String,
    address: AddressView,
    tags: Seq[String],
    speakers: Seq[String],
    languages: Seq[String],
    prices: Seq[PriceView],
    owner: String,
    status: String
)

object LiveEventView {
  implicit val format: Format[LiveEventView] = Json.format
}

case class AddressView(
    street: String,
    city: String,
    state: String,
    zip: String
)

object AddressView {
  implicit val format: Format[AddressView] = Json.format
}

case class PriceView(
    name: String,
    description: Option[String],
    value: BigDecimal,
    startDate: LocalDate,
    endDate: LocalDate,
    priceType: String
)

object PriceView {
  implicit val format: Format[PriceView] = Json.format
}
