package controllers

import javax.inject._
import play.api.mvc._
import services.SearchService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SearchController @Inject()(
    cc: ControllerComponents,
    searchService: SearchService
) extends AbstractController(cc) {

  def search(key: Option[String]): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    searchService
      .search(key)
      .map(
        liveEvents =>
          Ok(
            views.html.index("Welcome to Wintacky project!")(
              views.html.welcome(key)(views.html.cards(liveEvents))
            )
        )
      )

  }
}
