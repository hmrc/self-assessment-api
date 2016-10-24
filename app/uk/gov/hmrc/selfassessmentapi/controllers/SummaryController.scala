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

import play.api.hal.HalLink
import play.api.libs.json.Json._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceType
import uk.gov.hmrc.selfassessmentapi.controllers.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SummaryController extends BaseController with Links with SourceTypeSupport {

  override lazy val context: String = AppContext.apiGatewayLinkContext

  def handler(sourceType: SourceType, summaryTypeName: String): SummaryHandler[_] = {
    val summaryType = sourceType.summaryTypes.find(_.name == summaryTypeName)
    val handler = summaryType.flatMap(x => sourceHandler(sourceType).summaryHandler(x))
    handler.getOrElse(throw UnknownSummaryException(sourceType, summaryTypeName))
  }

  protected def createSummary(request: Request[JsValue], saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) = {
    handler(sourceType, summaryTypeName).create(saUtr, taxYear, sourceId, request.body) match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(invalidRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(invalidRequest(errors)))
          }
        }
      case Right(futOptId) => futOptId.map {
        case Some(id) => Created(halResource(Json.obj(), Set(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, id)))))
        case _ => notFound
      }
    }
  }

  protected def readSummary(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) = {
    handler(sourceType, summaryTypeName).findById(saUtr, taxYear, sourceId, summaryId) map {
      case Some(summary) =>
        Ok(halResource(summary, Set(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)))))
      case None => notFound
    }
  }

  protected def updateSummary(request: Request[JsValue], saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) = {
    handler(sourceType, summaryTypeName).update(saUtr, taxYear, sourceId, summaryId, request.body) match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(invalidRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(invalidRequest(errors)))
          }
        }
      case Right(optResult) => optResult.map {
        case true => Ok(halResource(obj(), Set(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)))))
        case false => notFound
      }
    }

  }


  protected def deleteSummary(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) = {
    handler(sourceType, summaryTypeName).delete(saUtr, taxYear, sourceId, summaryId) map {
      case true => NoContent
      case false => notFound
    }
  }

  protected def listSummaries(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) = {
    val svc = handler(sourceType, summaryTypeName)
    svc.find(saUtr, taxYear, sourceId) map { summaries =>
      val json = toJson(summaries.map(summary => halResource(summary.json,
        Set(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summary.id))))))

      Ok(halResourceList(svc.listName, json, sourceTypeAndSummaryTypeHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName)))
    }
  }

}

case class UnknownSummaryException(sourceType: SourceType, summaryTypeName: String) extends RuntimeException(s"summary: $summaryTypeName doesn't exist for source: ${sourceType.name}")
