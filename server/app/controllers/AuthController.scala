package controllers

import config.AuthConfiguration
import javax.inject.{Inject, Singleton}
import play.api.cache.SyncCacheApi
import play.api.mvc._
import services.TokenService
import utils.RandomUtil

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AuthController @Inject()(
    cc: ControllerComponents,
    authConfig: AuthConfiguration,
    tokenService: TokenService,
    cache: SyncCacheApi
) extends AbstractController(cc) {

  def login() = Action { implicit request: Request[AnyContent] =>
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

  def logout = Action {
    Redirect(
      s"https://${authConfig.domain}/v2/logout?client_id=${authConfig.clientId}&returnTo=${authConfig.logoutPage}"
    ).withNewSession
  }

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
                cache.set(idToken + "profile", true)
                Redirect(routes.HomeController.index())
                  .withSession(
                    "idToken"     -> idToken,
                    "accessToken" -> accessToken
                  )
            }
            .recover {
              case e: IllegalStateException => Unauthorized(e.getMessage)
            }
        }).getOrElse(Future.successful(BadRequest("No parameters supplied")))
      } else {
        Future.successful(BadRequest("Invalid state parameter"))
      }
  }

}
