package controllers

import javax.inject._
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

  val eventForm = Form(
    mapping(
      "name"        -> text,
      "description" -> text,
      "webSite"     -> text,
      "age"         -> text
    )(Event.apply)(Event.unapply)
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

case class Event(
    name: String,
    description: String,
    webSite: String,
    age: String,
)
