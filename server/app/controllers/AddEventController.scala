package controllers

import java.time.LocalDate

import javax.inject._
import models.{Address, LiveEvent, UserProfile}
import org.bson.types.ObjectId
import play.api.Logging
import play.api.mvc._
import repositories.LiveEventRepository
import services.AuthenticationControllerHelper

import scala.concurrent.Future

@Singleton
class AddEventController @Inject()(
    cc: ControllerComponents,
    authenticationControllerHelper: AuthenticationControllerHelper,
    liveEventRepository: LiveEventRepository
) extends AbstractController(cc)
    with Logging {

  def add: Action[AnyContent] = authenticationControllerHelper.authenticatedAsync {
    case (userProfile: UserProfile, request: Request[AnyContent]) =>
      logger.info("Add event request " + request.body.asFormUrlEncoded.getOrElse("NONE"))

      for {
        map   <- request.body.asFormUrlEncoded.map(_.mapValues(_.last))
        event <- parseFromMap(map, userProfile)
        _ = logger.info("Start save event")
      } yield {
        liveEventRepository.save(event)
      }

      Future.successful(Redirect(routes.CreateEventController.create()))
  }

  def parseFromMap(map: Map[String, String], owner: UserProfile): Option[LiveEvent] =
    for {
      name        <- map.get("name")
      startDate   <- map.get("startDate")
      endDate     <- map.get("endDate")
      description <- map.get("description")
      webSite     <- map.get("webSite")
      imageUrl    <- map.get("imageUrl")
      state       <- map.get("state")
      street      <- map.get("street")
      zip         <- map.get("zip")
      city        <- map.get("city")
      tags        <- map.get("tags").map(_.split(",").map(_.trim))
    } yield {
      LiveEvent(
        _id = ObjectId.get(),
        name = name,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        description = description,
        webSite = webSite,
        address = Address(
          street = street,
          city = city,
          state = state,
          zip = zip
        ),
        imageUrl = imageUrl,
        tags = tags,
        speakers = Nil,
        languages = Nil,
        prices = Nil,
        owner = owner.nickname
      )
    }
}
