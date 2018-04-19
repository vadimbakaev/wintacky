package controllers

import javax.inject._
import play.api.mvc._
import services.{AuthService, SearchService}
import utils.ClientParamsSanitizer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SearchController @Inject()(
    cc: ControllerComponents,
    searchService: SearchService,
    authService: AuthService
) extends AbstractController(cc) {

  def search(key: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val disinfectedKey = ClientParamsSanitizer(key)
    for {
      liveEvents       <- searchService.search(disinfectedKey)
      maybeAccessToken <- Future.successful(request.session.get(AuthController.AccessToken))
      maybeUser        <- maybeAccessToken.map(authService.recoverUser).getOrElse(Future.successful(None))
    } yield
      Ok(
        views.html.index("Welcome to Wintacky project!", maybeUser.isDefined)(
          views.html.welcome(disinfectedKey)(views.html.cards(liveEvents))
        )
      )
  }
}
