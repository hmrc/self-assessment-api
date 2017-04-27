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
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.BusinessDetailsConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors
import uk.gov.hmrc.selfassessmentapi.services.AuthenticationService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BaseResource extends BaseController {
  private val businessConnector = BusinessDetailsConnector
  private val authService = AuthenticationService
  private lazy val authIsEnabled = AppContext.authEnabled

  val logger: Logger = Logger(this.getClass)

  def withAuth(nino: Nino)(f: => Future[Result])
              (implicit hc: HeaderCarrier, reqHeader: RequestHeader): Future[Result] =
    if (authIsEnabled) performAuthCheck(nino)(f)
    else f

  /*
   * Retrieve the user's MTD reference number using the provided NINO.
   * This reference number is used to perform authorisation.
   */
  private def performAuthCheck(nino: Nino)(f: => Future[Result])
                              (implicit hc: HeaderCarrier, reqHeader: RequestHeader): Future[Result] =
    businessConnector.get(nino).flatMap { response =>
      response.status match {
        case 200 =>
          logger.debug(s"NINO to MTD reference lookup successful. Status Code ${response.status}.")
          authService.authorise(response.mtdId)(f)
        case 400 | 404 =>
          logger.debug(s"NINO to MTD reference lookup failed. Status Code ${response.status}")
          Future.successful(Unauthorized(Json.toJson(Errors.ClientNotSubscribed)))
        case _ => Future.successful(unhandledResponse(response.status, logger))
      }
    }
}
