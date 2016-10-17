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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.selfemployment

import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.{Adjustments, SourceType}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SourceTypes, TaxYear}
import uk.gov.hmrc.selfassessmentapi.controllers.controllers.validate
import uk.gov.hmrc.selfassessmentapi.controllers.{BaseController, GenericErrorResult, Links, ValidationErrorResult}
import uk.gov.hmrc.selfassessmentapi.repositories.live.SelfEmploymentRepository

import scala.concurrent.Future

object AdjustmentsController extends BaseController with Links {
  override val context: String = AppContext.apiGatewayLinkContext

  val repository = SelfEmploymentRepository()

  def update(utr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Action[JsValue] = Action.async(parse.json) { request =>
    validate[Adjustments, Boolean](request.body)(_ => Future.successful(true)) match {
      case Left(errorResult) => Future.successful {
        errorResult match {
          case GenericErrorResult(message) => BadRequest(Json.toJson(invalidRequest(message)))
          case ValidationErrorResult(errors) => BadRequest(Json.toJson(invalidRequest(errors)))
        }
      }
      case Right(_) => Future.successful(Ok(sourceIdHref(utr, taxYear, SourceType.SelfEmployments, sourceId)))
    }
  }

  def find(utr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Action[AnyContent] = Action.async { _ =>
    Future.successful {
      Ok(halResource(Json.toJson(Adjustments.example), sourceLinks(utr, taxYear, SourceTypes.SelfEmployments, sourceId)))
    }
  }
}
