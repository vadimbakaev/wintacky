package mappers

import java.time.LocalDate

import com.google.inject.Singleton
import models._
import org.bson.types.ObjectId

@Singleton
class LiveEvent2LiveEventView extends (LiveEvent => LiveEventView) {
  override def apply(v1: LiveEvent): LiveEventView = v1 match {
    case LiveEvent(
        _id: ObjectId,
        name: String,
        startDate: LocalDate,
        endDate: LocalDate,
        description: String,
        webSite: String,
        imageUrl: String,
        Address(
          street: String,
          city: String,
          state: String,
          zip: String
        ),
        tags: Seq[String],
        speakers: Seq[String],
        languages: Seq[String],
        prices: Seq[Price],
        owner: String,
        status: String
        ) =>
      LiveEventView(
        _id.toString,
        name,
        startDate,
        endDate,
        description,
        webSite,
        imageUrl,
        AddressView(street, city, state, zip),
        tags,
        speakers,
        languages,
        prices.flatMap(price => Price.unapply(price).map(PriceView.tupled)),
        owner,
        status
      )
  }
}
