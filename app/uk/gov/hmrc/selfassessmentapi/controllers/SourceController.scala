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
import play.api.mvc.Request
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SourceController extends BaseController with Links with SourceTypeSupport {

  override lazy val context: String = AppContext.apiGatewayContext

  protected def createSource(request: Request[JsValue], saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType) = {
    sourceHandler(sourceType).create(saUtr, taxYear, request.body) match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(message)
            case ValidationErrorResult(errors) => BadRequest(failedValidationJson(errors))
            case _ => BadRequest
          }
        }
      case Right(id) => id.map { sourceId => Created(halResource(obj(), sourceLinks(saUtr, taxYear, sourceType, sourceId))) }
    }
  }

  protected def readSource(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = {
    sourceHandler(sourceType).findById(saUtr, taxYear, sourceId) map {
      case Some(summary) => Ok(halResource(summary, sourceLinks(saUtr, taxYear, sourceType, sourceId)))
      case None => NotFound
    }
  }

  protected def updateSource(request: Request[JsValue], saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = {
    sourceHandler(sourceType).update(saUtr, taxYear, sourceId, request.body) match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(message)
            case ValidationErrorResult(errors) => BadRequest(failedValidationJson(errors))
            case _ => BadRequest
          }
        }
      case Right(result) => result.map {
        case true => Ok(halResource(obj(), sourceLinks(saUtr, taxYear, sourceType, sourceId)))
        case false => NotFound
      }
    }
  }

  protected def deleteSource(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = {
    sourceHandler(sourceType).delete(saUtr, taxYear, sourceId) map {
      case true => NoContent
      case false => NotFound
    }
  }

  protected def listSources(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType) = {
    val svc = sourceHandler(sourceType)
    svc.find(saUtr, taxYear) map { sources =>
      val json = toJson(sources.map(source => halResource(source.json,
        Set(HalLink("self", sourceIdHref(saUtr, taxYear, sourceType, source.id))))))
      Ok(halResourceList(svc.listName, json, sourceHref(saUtr, taxYear, sourceType)))
    }
  }

}
