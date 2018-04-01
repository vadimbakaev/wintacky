package controllers

import config.AuthConfiguration
import javax.inject.{Inject, Singleton}
import play.api.cache.SyncCacheApi
import play.api.mvc.{Action, _}
import services.TokenService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CallbackController @Inject()(
    cc: ControllerComponents,
    authConfig: AuthConfiguration,
    cache: SyncCacheApi,
    tokenService: TokenService
) extends AbstractController(cc) {

  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      if (stateOpt == cache.get("state")) {
        (for {
          code <- codeOpt
        } yield {
          tokenService
            .getToken(code)
            .map {
              case (idToken, accessToken) =>
                Redirect(routes.HomeController.index())
                  .withSession(
                    "idToken"     -> idToken,
                    "accessToken" -> accessToken
                  )
            }
            .recover {
              case ex: IllegalStateException => Unauthorized(ex.getMessage)
            }
        }).getOrElse(Future.successful(BadRequest("No parameters supplied")))
      } else {
        Future.successful(BadRequest("Invalid state parameter"))
      }
  }

}
