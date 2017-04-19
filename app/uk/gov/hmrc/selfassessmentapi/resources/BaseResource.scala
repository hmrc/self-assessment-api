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
import play.api.mvc.Result
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.config.MicroserviceAuthConnector
import uk.gov.hmrc.selfassessmentapi.connectors.BusinessDetailsConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models.MtdId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BaseResource extends BaseController with AuthorisedFunctions {
  override def authConnector: AuthConnector = MicroserviceAuthConnector
  private val businessConnector = BusinessDetailsConnector

  val logger: Logger

  def authorise(nino: Nino)(f: => Future[Result])(implicit hc: HeaderCarrier): Future[Result] =
    businessConnector.get(nino).flatMap { response =>
      response.status match {
        case 200 => authorise(response.mtdId)(f)
        case 400 | 404 => Future.successful(Unauthorized)
        case _ => Future.successful(unhandledResponse(response.status, logger))
      }
    }

  private def authorise(mtdId: Option[MtdId])(f: => Future[Result])(implicit hc: HeaderCarrier): Future[Result] =
    mtdId match {
      case Some(id) => authoriseAsClient(id)(f)
      case None => Future.successful(Unauthorized)
    }

  private def authoriseAsClient(mtdId: MtdId)(f: => Future[Result])(implicit hc: HeaderCarrier): Future[Result] =
    authorised(
      Enrolment("HMRC-MTD-IT")
        .withIdentifier("MTDITID", mtdId.mtdId)
        .withDelegatedAuthRule("mtd-it-auth")) {
      f
    } recoverWith /*authoriseAsFOA(f) recoverWith*/ unauthorised

  /*private def authoriseAsFOA(f: => Future[Result])
                            (implicit hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    case _: InsufficientEnrolments => authorised(
      Enrolment("HMRC-AS-AGENT")) {
      f
    }
  }*/

  private val unauthorised: PartialFunction[Throwable, Future[Status]] = {
    case e: AuthorisationException => {
      logger.debug(s"Client authorisation failed. Exception: [$e]")
      Future.successful(Unauthorized)
    }
  }
}
