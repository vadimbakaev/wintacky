package services

import com.google.inject.Inject
import config.AuthConfiguration
import javax.inject.Singleton
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TokenService @Inject()(
    config: AuthConfiguration,
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

}
