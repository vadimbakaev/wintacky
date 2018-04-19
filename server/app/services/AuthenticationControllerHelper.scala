package services

import controllers.{AuthController, routes}
import javax.inject.{Inject, Singleton}
import models.UserProfile
import play.api.cache.SyncCacheApi
import play.api.mvc.{Action, AnyContent, Request, Result, _}

import scala.concurrent.Future

@Singleton
class AuthenticationControllerHelper @Inject()(
    cache: SyncCacheApi,
    actionBuilder: DefaultActionBuilder,
    authService: AuthService
) {

  def authenticatedAsync(f: Request[AnyContent] => Future[Result]): Action[AnyContent] = actionBuilder.async {
    request =>
      request.session
        .get(AuthController.IdToken)
        .flatMap(idToken => cache.get[UserProfile](idToken + "profile"))
        .map(_ => f(request))
        .orElse(Some(Future.successful(Results.Redirect(routes.HomeController.index()))))
        .get
  }

}
