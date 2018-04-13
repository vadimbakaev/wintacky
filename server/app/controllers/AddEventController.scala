package controllers

import javax.inject._
import play.api.Logger
import play.api.mvc._
import services.AuthenticationControllerHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AddEventController @Inject()(
    cc: ControllerComponents,
    authenticationControllerHelper: AuthenticationControllerHelper
) extends AbstractController(cc) {

  def add: Action[AnyContent] = authenticationControllerHelper.authenticatedAsync {
    implicit request: Request[AnyContent] =>
      Logger.info("Add event request " + request.body.asFormUrlEncoded.getOrElse("NONE"))
      Future.successful(Redirect(routes.CreateEventController.create()))
  }
}
