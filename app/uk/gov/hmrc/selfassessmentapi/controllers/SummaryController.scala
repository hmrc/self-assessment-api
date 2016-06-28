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
import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import play.api.mvc.{Request, Result}
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SummaryController extends BaseController with Links with SourceTypeSupport {

  override lazy val context: String = AppContext.apiGatewayContext

  def handler(sourceType: SourceType, summaryTypeName: String): SummaryHandler[_] = {
    val summaryType = sourceType.summaryTypes.find(_.name == summaryTypeName)
    val handler = summaryType.flatMap(x => sourceHandler(sourceType).summaryHandler(x))
    handler.getOrElse(throw UnknownSummaryException(sourceType, summaryTypeName))
  }

  def createSummary(request: Request[JsValue], saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) = {
    handler(sourceType, summaryTypeName).create(saUtr, taxYear, sourceId, request.body) match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case ErrorResult(Some(message), _) => BadRequest(message)
            case ErrorResult(_, Some(errors)) => BadRequest(failedValidationJson(errors))
            case _ => BadRequest
          }
        }
      case Right(futOptId) => futOptId.map {
        case Some(id) => Created(halResource(obj(), Seq(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, id)))))
        case _ => BadRequest
      }
    }
  }

  def readSummary(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) = {
    handler(sourceType, summaryTypeName).findById(saUtr, taxYear, sourceId, summaryId) map {
      case Some(summary) =>
        Ok(halResource(toJson(summary), Seq(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)))))
      case None => NotFound
    }
  }

  def updateSummary(request: Request[JsValue], saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) = {
    handler(sourceType, summaryTypeName).update(saUtr, taxYear, sourceId, summaryId, request.body) match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case ErrorResult(Some(message), _) => BadRequest(message)
            case ErrorResult(_, Some(errors)) => BadRequest(failedValidationJson(errors))
            case _ => BadRequest
          }
        }
      case Right(optResult) => optResult.map {
        case true => Ok(halResource(obj(), Seq(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)))))
        case false => BadRequest
      }
    }

  }


  def deleteSummary(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String, summaryId: SummaryId) = {
    handler(sourceType, summaryTypeName).delete(saUtr, taxYear, sourceId, summaryId) map {
      case true => NoContent
      case false => NotFound
    }
  }

  def listSummaries(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) = {
    val svc = handler(sourceType, summaryTypeName)
    svc.find(saUtr, taxYear, sourceId) map { summaries =>
      val json = toJson(summaries.map(summary => halResource(summary.json,
        Seq(HalLink("self", sourceTypeAndSummaryTypeIdHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summary.id))))))

      Ok(halResourceList(svc.listName, json, sourceTypeAndSummaryTypeHref(saUtr, taxYear, sourceType, sourceId, summaryTypeName)))
    }
  }

}

case class UnknownSummaryException(sourceType: SourceType, summaryTypeName: String) extends RuntimeException(s"summary: $summaryTypeName doesn't exist for source: ${sourceType.name}")
