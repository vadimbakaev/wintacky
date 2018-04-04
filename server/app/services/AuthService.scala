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

@Singleton
class AuthService @Inject()(
    config: AuthConfiguration,
    cache: AsyncCacheApi,
    ws: WSClient
) {

  def getToken(code: String): Future[(String, String)] = {
    val tokenResponse = ws
      .url(s"https://${config.domain}/oauth/token")
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
        idToken     <- (response.json \ "id_token").asOpt[String]
        accessToken <- (response.json \ "access_token").asOpt[String]
      } yield {
        Future.successful((idToken, accessToken))
      }).getOrElse(Future.failed[(String, String)](new IllegalStateException("Tokens not sent")))
    }

  }

  def getUser(accessToken: Option[String]): Future[Option[JsValue]] =
    (for {
      accessToken <- accessToken
    } yield {
      cache
        .get[JsValue](s"accessToken_$accessToken")
        .flatMap {
          case None =>
            ws.url(s"https://${config.domain}/userinfo")
              .withQueryStringParameters("access_token" -> accessToken)
              .get()
              .flatMap {
                case ok if ok.status === 200 =>
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
    }).getOrElse(
      Future.successful(None)
    )

}
