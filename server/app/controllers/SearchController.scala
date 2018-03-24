package controllers

import javax.inject._
import models.LiveEvent
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class SearchController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def search() = Action { implicit request: Request[AnyContent] =>
    Ok(
      views.html.index("Welcome to Wintacky project!")(
        views.html.searchResult(
          views.html.cards(
            (1 to 25).map(
              index =>
                LiveEvent(
                  s"SCALAR $index",
                  "Warsaw, Poland",
                  "6 April 2018 - 7 April 2018",
                  "This is a wider card with supporting text below as a natural lead-in to additional content. This content is a little bit longer."
              )
            )
          )
        )
      )
    )
  }
}
