package controllers

import javax.inject._
import play.api.mvc._
import services.AuthService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class HomeController @Inject()(
    cc: ControllerComponents,
    authService: AuthService
) extends AbstractController(cc) {

  def index(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    for {
      maybeAccessToken <- Future.successful(request.session.get(AuthController.AccessToken))
      maybeUser        <- maybeAccessToken.map(authService.recoverUser).getOrElse(Future.successful(None))
    } yield
      Ok(
        views.html
          .index("Welcome to Wintacky project!", maybeUser)(views.html.welcome("")(views.html.cards(Nil)))
      )

  }
}
