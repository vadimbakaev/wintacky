package services

import controllers.routes
import javax.inject.{Inject, Singleton}
import play.api.cache.SyncCacheApi
import play.api.libs.json.JsValue
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
        .get("idToken")
        .flatMap(idToken => cache.get[JsValue](idToken + "profile"))
        .map(_ => f(request))
        .orElse(Some(Future.successful(Results.Redirect(routes.HomeController.index()))))
        .get
  }

}
