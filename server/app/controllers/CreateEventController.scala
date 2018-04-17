package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.AuthenticationControllerHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CreateEventController @Inject()(
    cc: ControllerComponents,
    authenticationControllerHelper: AuthenticationControllerHelper
) extends AbstractController(cc)
    with play.api.i18n.I18nSupport {

  import play.api.data.Form
  import play.api.data.Forms._

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
    implicit request: Request[AnyContent] =>
      Future.successful(
        Ok(
          views.html.index("Add new Event!", isLogged = true)(views.html.create(eventForm))
        )
      )
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
