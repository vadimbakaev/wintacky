package models.request.external

import play.api.libs.json.{Json, OFormat}

case class AccessTokenRequest(
    grant_type: String = "authorization_code",
    client_id: String,
    client_secret: String,
    redirect_uri: String,
    audience: String,
    code: String,
)

object AccessTokenRequest {
  implicit val format: OFormat[AccessTokenRequest] = Json.format
}
