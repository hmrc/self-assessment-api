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

package uk.gov.hmrc.selfassessmentapi.controllers.live.selfemployment

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.Allowances
import uk.gov.hmrc.selfassessmentapi.controllers.{BaseController, GenericErrorResult, Links, ValidationErrorResult}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SourceTypes, TaxYear}
import uk.gov.hmrc.selfassessmentapi.controllers.controllers.validate
import uk.gov.hmrc.selfassessmentapi.repositories.live.SelfEmploymentRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object AllowancesController extends BaseController with Links {
  override val context: String = AppContext.apiGatewayLinkContext

  val repository = SelfEmploymentRepository()

  def update(utr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Action[JsValue] = Action.async(parse.json) { request =>
    validate[Allowances, Boolean](request.body) { allowances =>
      repository.updateAllowances(utr, taxYear, sourceId, allowances)
    } match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(invalidRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(invalidRequest(errors)))
          }
        }
      case Right(id) => Future.successful(Ok(Json.toJson(sourceTypeAndSummaryTypeHref(utr, taxYear, SourceTypes.SelfEmployments, sourceId, "allowances"))))
    }
  }

  def find(utr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Action[AnyContent] = Action.async {
    repository.findAllowances(utr, taxYear, sourceId).map {
      case Some(adjustment) => Ok(halResource(Json.toJson(adjustment), sourceLinks(utr, taxYear, SourceTypes.SelfEmployments, sourceId)))
      case None => Ok(halResource(Json.toJson(Allowances(0, 0, 0, 0, 0, 0)), sourceLinks(utr, taxYear, SourceTypes.SelfEmployments, sourceId)))
    }
  }
}
