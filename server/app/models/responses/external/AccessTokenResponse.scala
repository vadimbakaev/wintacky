package models.responses.external

import play.api.libs.json.{Json, OFormat}

case class AccessTokenResponse(
    access_token: String,
    id_token: String,
    expires_in: Long,
    token_type: String
)

object AccessTokenResponse {
  implicit val format: OFormat[AccessTokenResponse] = Json.format
}
