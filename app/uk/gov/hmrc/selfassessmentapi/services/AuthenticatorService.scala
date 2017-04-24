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

package uk.gov.hmrc.selfassessmentapi.services

import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.MicroserviceAuthConnector
import uk.gov.hmrc.selfassessmentapi.models.MtdId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AuthenticatorService extends AuthorisedFunctions {
  override def authConnector: AuthConnector = MicroserviceAuthConnector

  private val logger = Logger(AuthenticatorService.getClass)

  def authorise(mtdId: Option[MtdId])(f: => Future[Result])
               (implicit hc: HeaderCarrier, reqHeader: RequestHeader): Future[Result] =
    mtdId match {
      case Some(id) => authoriseAsClient(id)(f)
      case None => Future.successful(Unauthorized)
    }

  private def authoriseAsClient(mtdId: MtdId)(f: => Future[Result])
                               (implicit hc: HeaderCarrier, requestHeader: RequestHeader): Future[Result] =
    authorised(
      Enrolment("HMRC-MTD-IT")
        .withIdentifier("MTDITID", mtdId.mtdId)
        .withDelegatedAuthRule("mtd-it-auth")) {
      logger.debug("Client authorisation succeeded as fully-authorised individual.")
      f
    } recoverWith authoriseAsFOA(f) recoverWith unauthorised

  private def authoriseAsFOA(f: => Future[Result])
                            (implicit hc: HeaderCarrier, reqHeader: RequestHeader): PartialFunction[Throwable, Future[Result]] = {
    case _: InsufficientEnrolments => authorised(
      Enrolment("HMRC-AS-AGENT")) {
      if (reqHeader.method == "GET") {
        logger.debug("Client authorisation failed. Attempt to GET as a filing-only agent.")
        Future.successful(Unauthorized)
      } else {
        logger.debug("Client authorisation succeeded as filing-only agent.")
        f
      }
    }
  }

  private def unauthorised(implicit requestHeader: RequestHeader): PartialFunction[Throwable, Future[Status]] = {
    case e: AuthorisationException => {
      logger.debug(s"Client authorisation failed. Exception: [$e]")
      Future.successful(Unauthorized)
    }
  }
}
