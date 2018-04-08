package services

import com.google.inject.Inject
import config.AuthConfiguration
import javax.inject.Singleton
import play.api.Logger
import play.api.cache.AsyncCacheApi
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import cats.implicits._
import models.request.external.AccessTokenRequest
import models.responses.external.AccessTokenResponse
import org.apache.http.HttpStatus

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

  def recoverUser(accessTokenOpt: Option[String]): Future[Option[JsValue]] =
    accessTokenOpt
      .map(
        accessToken => {
          val userKey = s"aToken_$accessToken"
          cache
            .get[JsValue](userKey)
            .flatMap {
              case None =>
                ws.url(userInfoUrl)
                  .withQueryStringParameters(AuthService.AccessTokenKey -> accessToken)
                  .get()
                  .flatMap {
                    case ok if ok.status === HttpStatus.SC_OK =>
                      Logger.info(s"User profile: ${ok.json}")
                      cache.set(userKey, ok.json)
                      Future.successful(Some(ok.json))
                    case fail @ _ =>
                      Logger.error(s"Unexpected response : ${fail.body}")
                      Future.successful(None)
                  }
              case value @ _ =>
                Future.successful(value)
            }
        }
      )
      .getOrElse(Future.successful(None))

}

object AuthService {
  val AccessTokenKey: String = "access_token"
  val IdTokenKey: String     = "id_token"
}
