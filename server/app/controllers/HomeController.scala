package controllers

import javax.inject._
import play.api.mvc._
import services.AuthService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(
    cc: ControllerComponents,
    authService: AuthService
) extends AbstractController(cc) {

  def index(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    authService
      .recoverUser(request.session.get("accessToken"))
      .map { maybeUser =>
        Ok(
          views.html
            .index("Welcome to Wintacky project!", maybeUser.isDefined)(views.html.welcome("")(views.html.cards(Nil)))
        )
      }

  }
}
