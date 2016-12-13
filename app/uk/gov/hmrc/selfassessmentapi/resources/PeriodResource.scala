/*
 * Copyright 2016 HM Revenue & Customs
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

import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.domain.PeriodContainer
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.resources.SelfEmploymentsResource._
import uk.gov.hmrc.selfassessmentapi.resources.models.SourceType.SourceType
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.services.PeriodService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PeriodResource[ID <: String, P <: Period, PC <: PeriodContainer[P, PC, PD], PD <: PeriodicData] {
  implicit val periodFormat: Format[P]
  implicit val periodicDataFormat: Format[PD]

  val periodService: PeriodService[ID, P, PC, PD]
  val sourceType: SourceType

  private lazy val featureSwitch = FeatureSwitchAction(sourceType, "periods")

  def createPeriod(nino: Nino, sourceId: ID): Action[JsValue] = featureSwitch.asyncFeatureSwitch { request =>
    validate[P, Either[Error, PeriodId]](request.body) { period =>
      periodService.createPeriod(nino, sourceId, period)
    } match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
          }
        }
      case Right(result) => result.map {
        case Right(periodId) => Created.withHeaders(LOCATION -> s"/self-assessment/ni/$nino/${sourceType.toString}/$sourceId/periods/$periodId")
        case Left(error) =>
          if (error.code == ErrorCode.NOT_FOUND.toString) NotFound
          else Forbidden(Json.toJson(Errors.businessError(error)))
      }
    }
  }

  def updatePeriod(nino: Nino, id: ID, periodId: PeriodId): Action[JsValue] = featureSwitch.asyncFeatureSwitch { request =>
    validate[PD, Boolean](request.body) {
      periodService.updatePeriod(nino, id, periodId, _)
    } match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
          }
        }
      case Right(result) => result.map {
        case true => NoContent
        case false => NotFound
      }
    }
  }

  def retrievePeriod(nino: Nino, id: ID, periodId: PeriodId): Action[AnyContent] = featureSwitch.asyncFeatureSwitch {
    periodService.retrievePeriod(nino, id, periodId) map {
      case Some(period) => Ok(Json.toJson(period))
      case None => NotFound
    }
  }

  def retrievePeriods(nino: Nino, id: ID): Action[AnyContent] = featureSwitch.asyncFeatureSwitch {
    periodService.retrieveAllPeriods(nino, id).map { periods => Ok(Json.toJson(periods)) }
  }
}
