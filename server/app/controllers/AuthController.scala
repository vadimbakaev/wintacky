package controllers

import config.AuthConfiguration
import javax.inject.{Inject, Singleton}
import play.api.cache.SyncCacheApi
import play.api.mvc._
import services.AuthService
import utils.RandomUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AuthController @Inject()(
    cc: ControllerComponents,
    authConfig: AuthConfiguration,
    tokenService: AuthService,
    cache: SyncCacheApi
) extends AbstractController(cc) {

  def login(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val state = RandomUtil.alphanumeric()
    cache.set("state", state)

    val query =
      s"""
          |authorize?
          |client_id=${authConfig.clientId}
          |&redirect_uri=${authConfig.callbackURL}
          |&response_type=code
          |&scope=openid profile
          |&audience=${authConfig.audience}
          |&state=$state
       """.stripMargin.replaceAll("\n", "")

    Redirect(s"https://${authConfig.domain}/$query")
  }

  def logout: Action[AnyContent] = Action {
    Redirect(
      s"https://${authConfig.domain}/v2/logout?client_id=${authConfig.clientId}&returnTo=${authConfig.logoutPage}"
    ).withNewSession
  }

  def callback(code: String, state: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      if (cache.get("state").contains(state)) {
        tokenService
          .recoverToken(code)
          .map {
            case (idToken, accessToken) =>
              Redirect(routes.HomeController.index())
                .withSession(
                  "idToken"     -> idToken,
                  "accessToken" -> accessToken
                )
          }
          .recover {
            case e: IllegalStateException => Unauthorized(e.getMessage)
          }
      } else {
        Future.successful(BadRequest("Invalid state parameter"))
      }
  }

}
