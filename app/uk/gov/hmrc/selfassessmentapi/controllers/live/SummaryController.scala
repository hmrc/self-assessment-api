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

package uk.gov.hmrc.selfassessmentapi.controllers.live

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.ErrorNotImplemented
import uk.gov.hmrc.selfassessmentapi.{FeatureSwitchAction, domain}
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments

import scala.concurrent.Future
object SummaryController extends uk.gov.hmrc.selfassessmentapi.controllers.SummaryController with SourceTypeSupport {

  val supportedSummaryTypes =
    Map[SourceType, Set[String]](SelfEmployments -> Set(domain.selfemployment.SummaryTypes.Incomes.name)).withDefaultValue(Set())

  private def withSupportedType(sourceType: SourceType, summaryTypeName: String)(f: => Future[Result]) =
    FeatureSwitchAction(sourceType).async {
      supportedSummaryTypes(sourceType).contains(summaryTypeName) match {
        case true => f
        case false => Future.successful(NotImplemented(toJson(ErrorNotImplemented)))
      }
    }

  private def withSupportedTypeAndBody(sourceType: SourceType, summaryTypeName: String)(f: Request[JsValue] => Future[Result]) =
    FeatureSwitchAction(sourceType).async(parse.json) {
      request =>
        supportedSummaryTypes(sourceType).contains(summaryTypeName) match {
          case true => f(request)
          case false => Future.successful(NotImplemented(toJson(ErrorNotImplemented)))
        }
    }

  def create(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId,
             summaryTypeName: String) = withSupportedTypeAndBody(sourceType , summaryTypeName) {
    request => super.createSummary(request, saUtr, taxYear, sourceType, sourceId, summaryTypeName)
  }

  def read(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String,
           summaryId: SummaryId) = withSupportedType(sourceType, summaryTypeName) {
    super.readSummary(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
  }

  def update(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId,
             summaryTypeName: String, summaryId: SummaryId) = withSupportedTypeAndBody(sourceType , summaryTypeName) {
    request => super.updateSummary(request, saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String,
             summaryId: SummaryId) = withSupportedType(sourceType, summaryTypeName) {
    super.deleteSummary(saUtr, taxYear, sourceType, sourceId, summaryTypeName, summaryId)
  }

  def list(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryTypeName: String) = withSupportedType(sourceType, summaryTypeName) {
    super.listSummaries(saUtr, taxYear, sourceType, sourceId, summaryTypeName)
  }
}
