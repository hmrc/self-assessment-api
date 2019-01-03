/*
 * Copyright 2019 HM Revenue & Customs
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

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{ActionBuilder, _}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.contexts.{AuthContext, Individual}
import uk.gov.hmrc.selfassessmentapi.models.SourceType.SourceType
import uk.gov.hmrc.selfassessmentapi.services.AuthorisationService

import scala.concurrent.Future

trait BaseResource extends BaseController {
  val appContext: AppContext
  val authService: AuthorisationService

  val logger: Logger = Logger(this.getClass.getSimpleName)
  private lazy val authIsEnabled = appContext.authEnabled
  private lazy val featureSwitch = FeatureSwitch(appContext.featureSwitch)

  def AuthAction(nino: Nino) = new ActionRefiner[Request, AuthRequest] {
    override protected def refine[A](request: Request[A]): Future[Either[Result, AuthRequest[A]]] =
      if (authIsEnabled) {
        implicit val ev: Request[A] = request
        authService.authCheck(nino) map {
          case Right(authContext) => Right(new AuthRequest(authContext, request))
          case Left(authError) => Left(authError)
        }
      } else Future.successful(Right(new AuthRequest(Individual, request)))
  }

  def FeatureSwitchAction(source: SourceType, summary: Option[String] = None) =
    new ActionBuilder[Request] with ActionFilter[Request] {
      override protected def filter[A](request: Request[A]): Future[Option[Result]] =
        Future {
          if (featureSwitch.isEnabled(source, summary)) None
          else Some(NotFound)
        }
    }

  def APIAction(nino: Nino, source: SourceType, summary: Option[String] = None): ActionBuilder[AuthRequest] =
    FeatureSwitchAction(source, summary) andThen AuthAction(nino)


  def getRequestDateTimestamp(implicit request: AuthRequest[_]) = {
    val requestTimestampHeader = "X-Request-Timestamp"
    val requestTimestamp = request.headers.get(requestTimestampHeader) match {
      case Some(timestamp) if timestamp.trim.length > 0 => timestamp.trim
      case _ =>
        logger.warn(s"$requestTimestampHeader header is not passed or is empty in the request.")
        DateTime.now().toString()
    }
    requestTimestamp
  }

  def exceptionHandling(implicit req: Request[_]): PartialFunction[Throwable, Future[Result]] = {
    case ex =>
      logger.warn(s"when requesting ${req.uri} an uncaught error occurred.", ex)
      Future.successful(InternalServerError)
  }
}

class AuthRequest[A](val authContext: AuthContext, request: Request[A]) extends WrappedRequest[A](request)
