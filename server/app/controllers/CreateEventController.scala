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
) extends AbstractController(cc) {

  def create: Action[AnyContent] = authenticationControllerHelper.authenticatedAsync {
    implicit request: Request[AnyContent] =>
      Future.successful(
        Ok(
          views.html.index("Add new Event!", isLogged = true)(views.html.create())
        )
      )
  }
}
