package controllers

import javax.inject._
import play.api.mvc._
import services.{AuthService, SearchService}
import utils.ClientParamsSanitizer

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SearchController @Inject()(
    cc: ControllerComponents,
    searchService: SearchService,
    authService: AuthService
) extends AbstractController(cc) {

  def search(key: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val disinfectedKey = ClientParamsSanitizer(key)
    for {
      liveEvents <- searchService.search(disinfectedKey)
      maybeUser  <- authService.recoverUser(request.session.get("accessToken"))
    } yield
      Ok(
        views.html.index("Welcome to Wintacky project!", maybeUser.isDefined)(
          views.html.welcome(disinfectedKey)(views.html.cards(liveEvents))
        )
      )
  }
}
