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
import org.apache.http.HttpStatus

@Singleton
class AuthService @Inject()(
    config: AuthConfiguration,
    cache: AsyncCacheApi,
    ws: WSClient
) {

  private lazy val tokenUrl    = s"https://${config.domain}/oauth/token"
  private lazy val userInfoUrl = s"https://${config.domain}/userinfo"

  def recoverToken(code: String): Future[(String, String)] = {

    val tokenResponse = ws
      .url(tokenUrl)
      .withHttpHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .post(
        Json.obj(
          "client_id"     -> config.clientId,
          "client_secret" -> config.secret,
          "redirect_uri"  -> config.callbackURL,
          "code"          -> code,
          "grant_type"    -> "authorization_code",
          "audience"      -> config.audience
        )
      )

    tokenResponse.flatMap { response =>
      (for {
        idToken     <- (response.json \ AuthService.IdTokenKey).asOpt[String]
        accessToken <- (response.json \ AuthService.AccessTokenKey).asOpt[String]
      } yield {
        Future.successful((idToken, accessToken))
      }).getOrElse(Future.failed[(String, String)](new IllegalStateException("Tokens not sent")))
    }

  }

  def recoverUser(accessToken: Option[String]): Future[Option[JsValue]] =
    accessToken
      .map(
        accessToken =>
          cache
            .get[JsValue](s"accessToken_$accessToken")
            .flatMap {
              case None =>
                ws.url(userInfoUrl)
                  .withQueryStringParameters(AuthService.AccessTokenKey -> accessToken)
                  .get()
                  .flatMap {
                    case ok if ok.status === HttpStatus.SC_OK =>
                      Logger.info(s"User profile: ${ok.json}")
                      cache.set(s"accessToken_$accessToken", ok.json)
                      Future.successful(Some(ok.json))
                    case fail =>
                      Logger.error(s"Unexpected response : ${fail.body}")
                      Future.successful(None)
                  }
              case value =>
                Future.successful(value)
          }
      )
      .getOrElse(Future.successful(None))

}

object AuthService {
  val AccessTokenKey: String = "access_token"
  val IdTokenKey: String     = "id_token"
}
