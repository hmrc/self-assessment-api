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

import play.api.hal.HalLink
import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureConfig}
import uk.gov.hmrc.selfassessmentapi.controllers.ErrorNotImplemented
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SourceType.UnearnedIncomes
import uk.gov.hmrc.selfassessmentapi.domain.{SourceId, SourceType, TaxYear}

import scala.concurrent.Future

object SourceController extends uk.gov.hmrc.selfassessmentapi.controllers.SourceController with SourceTypeSupport {

  val supportedSourceTypes: Set[SourceType] = Set(SelfEmployments, UnearnedIncomes)

  private def withSupportedTypeAndBody(sourceType: SourceType)(f: Request[JsValue] => Future[Result]) =
    FeatureSwitchAction(sourceType).async(parse.json) {
      implicit request => supportedSourceTypes.contains(sourceType) match {
        case true => f(request)
        case false => Future.successful(NotImplemented(toJson(ErrorNotImplemented)))
      }
    }

  private def withSupportedType(sourceType: SourceType)(f: => Future[Result]) =
    FeatureSwitchAction(sourceType).async {
      supportedSourceTypes.contains(sourceType) match {
        case true => f
        case false => Future.successful(NotImplemented(toJson(ErrorNotImplemented)))
      }
    }

  def create(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType) = withSupportedTypeAndBody(sourceType) {
    request => super.createSource(request, saUtr, taxYear, sourceType)
  }

  def read(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = withSupportedType(sourceType) {
    super.readSource(saUtr, taxYear, sourceType, sourceId)
  }

  def update(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = withSupportedTypeAndBody(sourceType) {
    request => super.updateSource(request, saUtr, taxYear, sourceType, sourceId)
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = withSupportedType(sourceType) {
    super.deleteSource(saUtr, taxYear, sourceType, sourceId)
  }

  def list(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType) = withSupportedType(sourceType) {
    super.listSources(saUtr, taxYear, sourceType)
  }

  override def buildSourceHalLinks(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId): Set[HalLink] = {
    sourceLinks(saUtr, taxYear, sourceType, sourceId).filter { halLink =>
      if (AppContext.featureSwitch.isDefined) FeatureConfig(AppContext.featureSwitch.get).isSummaryEnabled(sourceType.name, halLink.rel)
      else false
    }
  }
}
