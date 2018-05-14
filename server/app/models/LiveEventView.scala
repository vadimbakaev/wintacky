package models

import java.time.LocalDate

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

case class AddressView(
    street: String,
    city: String,
    state: String,
    zip: String
)

case class PriceView(
    name: String,
    description: Option[String],
    value: BigDecimal,
    startDate: LocalDate,
    endDate: LocalDate,
    priceType: String
)
