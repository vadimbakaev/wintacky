package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class AuthConfiguration @Inject()(
    configuration: Configuration
) {

  lazy val secret: String      = configuration.get[String]("auth0.clientSecret")
  lazy val clientId: String    = configuration.get[String]("auth0.clientId")
  lazy val callbackURL: String = configuration.get[String]("auth0.callbackURL")
  lazy val domain: String      = configuration.get[String]("auth0.domain")
  lazy val audience: String    = configuration.get[String]("auth0.audience")
  lazy val logoutPage: String  = configuration.get[String]("auth0.logoutPage")

}
