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

  private[this] lazy val baseUrl: String = s"https://${authConfig.domain}"

  def login(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val state = RandomUtil.alphanumeric()
    cache.set(AuthController.StateKey, state)

    val queryString: Map[String, Seq[String]] = Map(
      AuthController.ClientIdQueryKey     -> Seq(authConfig.clientId),
      AuthController.RedirectUriQueryKey  -> Seq(authConfig.callbackURL),
      AuthController.AudienceQueryKey     -> Seq(authConfig.audience),
      AuthController.ResponseTypeQueryKey -> Seq("code"),
      AuthController.ScopeQueryKey        -> Seq("openid profile"),
      AuthController.StateQueryKey        -> Seq(state)
    )

    Redirect(baseUrl + "/authorize", queryString)
  }

  def logout: Action[AnyContent] = Action {

    val queryString: Map[String, Seq[String]] = Map(
      AuthController.ClientIdQueryKey -> Seq(authConfig.clientId),
      AuthController.ReturnToQueryKey -> Seq(authConfig.logoutPage)
    )

    Redirect(baseUrl + "/v2/logout", queryString).withNewSession
  }

  def callback(code: String, state: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      if (cache.get(AuthController.StateKey).contains(state)) {
        tokenService
          .recoverToken(code)
          .map {
            case (idToken, accessToken) =>
              Redirect(routes.HomeController.index()).withSession("idToken" -> idToken, "accessToken" -> accessToken)
          }
          .recover {
            case e => Unauthorized(e.getMessage)
          }
      } else {
        Future.successful(BadRequest("Invalid state parameter"))
      }
  }

}

object AuthController {
  val StateKey: String = "state"

  val ClientIdQueryKey: String     = "client_id"
  val RedirectUriQueryKey: String  = "redirect_uri"
  val AudienceQueryKey: String     = "audience"
  val ResponseTypeQueryKey: String = "response_type"
  val ScopeQueryKey: String        = "scope"
  val StateQueryKey: String        = "state"
  val ReturnToQueryKey: String     = "returnTo"
}
