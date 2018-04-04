package controllers

import javax.inject._
import play.api.mvc._
import services.{AuthService, SearchService}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SearchController @Inject()(
    cc: ControllerComponents,
    searchService: SearchService,
    authService: AuthService
) extends AbstractController(cc) {

  def search(key: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    searchService
      .search(key)
      .flatMap(
        liveEvents =>
          authService
            .getUser(request.session.get("accessToken"))
            .map { maybeUser =>
              Ok(
                views.html.index("Welcome to Wintacky project!", maybeUser.isDefined)(
                  views.html.welcome(key)(views.html.cards(liveEvents))
                )
              )
          }
      )
  }
}
