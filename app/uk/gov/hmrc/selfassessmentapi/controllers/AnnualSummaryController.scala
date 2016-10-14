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

package uk.gov.hmrc.selfassessmentapi.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SourceType, TaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



trait AnnualSummaryController extends BaseController with Links with SourceTypeSupport {
  override val context: String = AppContext.apiGatewayLinkContext

  def handler(sourceType: SourceType, annualSummaryName: String): AnnualSummaryHandler[_] = {
    val annualSummaryType = sourceType.propertyTypes.find(_.name == annualSummaryName)
    val handler = annualSummaryType.flatMap(x => sourceHandler(sourceType).annualSummaryHandler(x))
    handler.getOrElse(throw UnknownSummaryException(sourceType, annualSummaryName))
  }

  def createOrUpdateAnnualSummary(request: Request[JsValue], saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, annualSummaryTypeName: String) = {
    handler(sourceType, annualSummaryTypeName).createOrUpdate(saUtr, taxYear, sourceId, request.body) match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(invalidRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(invalidRequest(errors)))
          }
        }
      case Right(id) => Future.successful(Ok(Json.toJson(sourceTypeAndSummaryTypeHref(saUtr, taxYear, sourceType, sourceId, annualSummaryTypeName))))
    }
  }

  def findAnnualSummary(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, annualSummaryTypeName: String) = {
    handler(sourceType, annualSummaryTypeName).find(saUtr, taxYear, sourceId) map {
      case Some(summary) => Ok(halResource(Json.toJson(summary), sourceLinks(saUtr, taxYear, sourceType, sourceId)))
      case None => notFound
    }
  }
}
