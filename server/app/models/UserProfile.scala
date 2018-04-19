package models

import play.api.libs.json.{Json, OFormat}

case class UserProfile(
    sub: String,
    given_name: String,
    family_name: String,
    nickname: String,
    name: String,
    picture: String,
    gender: Option[String],
    locale: String,
    updated_at: String,
)

object UserProfile {
  implicit val format: OFormat[UserProfile] = Json.format
}
