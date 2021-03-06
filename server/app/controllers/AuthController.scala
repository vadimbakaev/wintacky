package controllers

import cats.implicits._
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

  private[this] lazy val baseUrl: String = s"https://${authConfig.domain}"

  def login(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val state = RandomUtil.alphanumeric()
    cache.set(AuthController.CacheStateKey, state)

    val queryString: Map[String, Seq[String]] = Map(
      AuthController.ClientIdKey     -> Seq(authConfig.clientId),
      AuthController.RedirectUriKey  -> Seq(authConfig.callbackURL),
      AuthController.ResponseTypeKey -> Seq("code"),
      AuthController.ScopeKey        -> Seq("openid profile"),
      AuthController.StateKey        -> Seq(state)
    )

    Redirect(baseUrl + "/authorize", queryString)
  }

  def logout: Action[AnyContent] = Action {

    val queryString: Map[String, Seq[String]] = Map(
      AuthController.ClientIdKey -> Seq(authConfig.clientId),
      AuthController.ReturnToKey -> Seq(authConfig.logoutPage)
    )

    Redirect(baseUrl + "/v2/logout", queryString).withNewSession
  }

  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      if (cache.get[String](AuthController.CacheStateKey) === stateOpt) {
        codeOpt
          .map { code =>
            tokenService
              .recoverToken(code)
              .map {
                case (idToken, accessToken) =>
                  tokenService
                    .recoverUser(accessToken)
                    .onComplete(tryMaybe => tryMaybe.foreach(_.foreach(cache.set(idToken + "profile", _))))

                  Redirect(routes.HomeController.index())
                    .withSession(AuthController.IdToken -> idToken, AuthController.AccessToken -> accessToken)
              }
              .recover {
                case e @ _ => Unauthorized(e.getMessage)
              }
          }
          .getOrElse(Future.successful(BadRequest("No parameters supplied")))
      } else {
        Future.successful(BadRequest("Invalid state parameter"))
      }
  }

}

object AuthController {
  val CacheStateKey: String   = "cache_state"
  val ClientIdKey: String     = "client_id"
  val RedirectUriKey: String  = "redirect_uri"
  val AudienceKey: String     = "audience"
  val ResponseTypeKey: String = "response_type"
  val ScopeKey: String        = "scope"
  val StateKey: String        = "state"
  val ReturnToKey: String     = "returnTo"
  val IdToken: String         = "idToken"
  val AccessToken: String     = "AccessToken"
}
