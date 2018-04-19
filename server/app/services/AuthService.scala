package services

import config.AuthConfiguration
import javax.inject.{Inject, Singleton}
import models.UserProfile
import models.request.external.AccessTokenRequest
import models.responses.external.AccessTokenResponse
import play.api.Logger
import play.api.cache.AsyncCacheApi
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AuthService @Inject()(
    config: AuthConfiguration,
    cache: AsyncCacheApi,
    ws: WSClient
) {

  private[this] lazy val tokenUrl    = s"https://${config.domain}/oauth/token"
  private[this] lazy val userInfoUrl = s"https://${config.domain}/userinfo"

  def recoverToken(code: String): Future[(String, String)] =
    ws.url(tokenUrl)
      .withHttpHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .post(
        Json.toJson(
          AccessTokenRequest(
            client_id = config.clientId,
            client_secret = config.secret,
            redirect_uri = config.callbackURL,
            audience = config.audience,
            code = code
          )
        )
      )
      .map { response =>
        response.json
          .validate[AccessTokenResponse]
          .fold(
            _ => throw new IllegalStateException("Tokens not sent"),
            tokenResponse => (tokenResponse.id_token, tokenResponse.access_token)
          )
      }

  def recoverUser(accessToken: String): Future[Option[UserProfile]] =
    cache
      .get[UserProfile](accessToken)
      .flatMap {
        case value @ Some(_) =>
          Future.successful(value)
        case _ =>
          ws.url(userInfoUrl)
            .withQueryStringParameters(AuthService.AccessTokenKey -> accessToken)
            .get()
            .map { response =>
              response.json
                .validate[UserProfile]
                .fold(
                  invalid => {
                    Logger.error(s"Unexpected response : ${response.body} $invalid")
                    None
                  },
                  userProfile => {
                    cache.set(accessToken, userProfile)
                    Some(userProfile)
                  }
                )
            }
      }

}

object AuthService {
  val AccessTokenKey: String = "access_token"
  val IdTokenKey: String     = "id_token"
}
