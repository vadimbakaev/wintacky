package controllers

import javax.inject.{Inject, Singleton}
import models.UserProfile
import play.api.mvc._
import services.AuthenticationControllerHelper
import play.api.data.Form
import play.api.data.Forms._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CreateEventController @Inject()(
    cc: ControllerComponents,
    authenticationControllerHelper: AuthenticationControllerHelper
) extends AbstractController(cc)
    with play.api.i18n.I18nSupport {

  private[this] val eventForm = Form(
    mapping(
      "name"        -> text,
      "startDate"   -> text,
      "endDate"     -> text,
      "description" -> text,
      "webSite"     -> text,
      "imageUrl"    -> text,
      "state"       -> text,
      "street"      -> text,
      "zip"         -> text,
      "city"        -> text
    )(LiveEventForm.apply)(LiveEventForm.unapply)
  )

  def create: Action[AnyContent] = authenticationControllerHelper.authenticatedAsync {
    (userProfile: UserProfile, request: Request[AnyContent]) =>
      {
        implicit val req: Request[AnyContent] = request
        Future.successful(
          Ok(
            views.html.index("Add new Event!", Some(userProfile))(views.html.create(eventForm))
          )
        )
      }

  }
}

case class LiveEventForm(
    name: String,
    startDate: String,
    endDate: String,
    description: String,
    webSite: String,
    imageUrl: String,
    state: String,
    street: String,
    zip: String,
    city: String
)
