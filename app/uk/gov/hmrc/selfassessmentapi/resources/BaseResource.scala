/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.Logger
import play.api.mvc.{ActionBuilder, _}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.SourceType.SourceType
import uk.gov.hmrc.selfassessmentapi.services.AuthenticationService
import uk.gov.hmrc.selfassessmentapi.services.AuthenticationService.AuthErrorResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthRequest[A](val authContext: AuthContext, request: Request[A]) extends WrappedRequest[A](request)

trait BaseResource extends BaseController {
  val logger: Logger = Logger(this.getClass)

  private val authService = AuthenticationService
  private lazy val authIsEnabled = AppContext.authEnabled

  def AuthAction(nino: Nino) = new ActionRefiner[Request, AuthRequest] {
    override protected def refine[A](request: Request[A]): Future[Either[AuthErrorResult, AuthRequest[A]]] =
      Future.successful {
        if (authIsEnabled) {
          implicit val ev: Request[A] = request
          authService.authCheck(nino) map {
            case Right(authContext) => Right(new AuthRequest(authContext, request))
            case Left(authError) => Left(authError)
          }
        } else Future.successful(Right(new AuthRequest(AuthContext(isFOA = false), request)))
      } flatMap identity
  }

  def FeatureSwitchAction(source: SourceType, summary: Option[String] = None) = new ActionBuilder[Request] {
    private val isFeatureEnabled = FeatureSwitch(AppContext.featureSwitch).isEnabled(source, summary)

    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      Future.successful {
        if (isFeatureEnabled) block(request) else Future.successful(NotFound)
      } flatMap identity
    }
  }

  def APIAction(nino: Nino, source: SourceType, summary: Option[String] = None): ActionBuilder[AuthRequest] =
    FeatureSwitchAction(source, summary) andThen AuthAction(nino)
}
