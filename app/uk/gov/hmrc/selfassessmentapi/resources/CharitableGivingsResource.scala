/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.CharitableGivingsConnector
import uk.gov.hmrc.selfassessmentapi.models.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceType, TaxYear, des}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.EmptyResponse
import uk.gov.hmrc.selfassessmentapi.services.AuthorisationService

import scala.concurrent.ExecutionContext.Implicits._

object CharitableGivingsResource extends CharitableGivingsResource {
  val charitableGivingsConnector = CharitableGivingsConnector
  val appContext = AppContext
  val authService = AuthorisationService
}

trait CharitableGivingsResource extends BaseResource {

  val charitableGivingsConnector: CharitableGivingsConnector

  def updatePayments(nino: Nino, taxYear: TaxYear): Action[JsValue] =
    APIAction(nino, SourceType.CharitableGivings).async(parse.json) { implicit request =>
      Logger.debug(s"[CharitableGivingsResource][updatePayments] Update gift aid payments for nino: $nino and tax year: $taxYear")
      validate[CharitableGivings, EmptyResponse](request.body) { charitableGivings =>
        charitableGivingsConnector.update(nino, taxYear, des.charitablegiving.CharitableGivings.from(charitableGivings))
      } map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right(response) =>
          response.filter {
            case 204 => Logger.debug(s"[CharitableGivingsResource][updatePayments] Update gift aid payments " +
              s" is successful for nino: $nino and tax year: $taxYear")
              NoContent
            case 400 => Logger.warn(s"[CharitableGivingsResource][updatePayments] Update gift aid payments " +
              s" is unsuccessful for nino: $nino and tax year: $taxYear due to ${Errors.desErrorToApiError(response.json)}")
              BadRequest(Errors.desErrorToApiError(response.json))
            case 404 => Logger.warn(s"[CharitableGivingsResource][updatePayments] Update gift aid payments " +
              s" is unsuccessful for nino: $nino and tax year: $taxYear due to ${Errors.desErrorToApiError(response.json)}")
              NotFound(Errors.desErrorToApiError(response.json))
            case _ => Logger.warn(s"[CharitableGivingsResource][updatePayments] Update gift aid payments " +
              s" is unsuccessful for nino: $nino and tax year: $taxYear due to ${Errors.desErrorToApiError(response.json)}")
              InternalServerError(Errors.desErrorToApiError(response.json))
          }
      } recoverWith exceptionHandling
    }

  def retrievePayments(nino: Nino, taxYear: TaxYear): Action[AnyContent] =
    APIAction(nino, SourceType.CharitableGivings).async { implicit request =>
      Logger.debug(s"[CharitableGivingsResource][retrievePayments] Retrieve gift aid payments for nino: $nino and tax year: $taxYear")
      charitableGivingsConnector.get(nino, taxYear).map { response =>
        response.filter {
          case 200 =>
            response.payments match {
              case Some(payments) =>
                Logger.debug(s"[CharitableGivingsResource][retrievePayments] Retrieve gift aid payments is successful for nino: $nino and tax year: $taxYear")
                Ok(Json.toJson(payments))
              case None => NotFound
            }
          case 400 => Logger.warn(s"[CharitableGivingsResource][retrievePayments] Update gift aid payments " +
            s" is unsuccessful for nino: $nino and tax year: $taxYear due to ${Errors.desErrorToApiError(response.json)}")
            BadRequest(Errors.desErrorToApiError(response.json))
          case 404 =>
            Logger.warn(s"[CharitableGivingsResource][retrievePayments] Update gift aid payments " +
              s" is unsuccessful for nino: $nino and tax year: $taxYear due to ${Errors.desErrorToApiError(response.json)}")
            NotFound(Errors.desErrorToApiError(response.json))
          case _ => Logger.warn(s"[CharitableGivingsResource][retrievePayments] Update gift aid payments " +
            s" is unsuccessful for nino: $nino and tax year: $taxYear due to ${Errors.desErrorToApiError(response.json)}")
            InternalServerError(Errors.desErrorToApiError(response.json))
        }
      } recoverWith exceptionHandling
    }
}
