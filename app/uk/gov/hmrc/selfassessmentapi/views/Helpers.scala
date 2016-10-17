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

package uk.gov.hmrc.selfassessmentapi.views

import scala.xml.PCData

import play.api.hal.HalLink
import play.api.hal.Hal._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.json.Json._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.controllers.{HalSupport, Links}
import uk.gov.hmrc.selfassessmentapi.controllers.api._

object Helpers extends HalSupport with Links {

  override val context: String = AppContext.apiGatewayLinkContext

  private val featureSwitch = FeatureSwitch(AppContext.featureSwitch)

  val enabledSourceTypes: Set[SourceType] = SourceTypes.types.filter(featureSwitch.isEnabled)

  def enabledSummaries(sourceType: SourceType): Set[SummaryType] =
    sourceType.summaryTypes.filter(summary => featureSwitch.isEnabled(sourceType, summary.name))

  def enabledYearProperties(sourceType: SourceType): Set[YearPropertyType] = {
    sourceType.propertyTypes.filter(prop => featureSwitch.isEnabled(sourceType, prop.name))
  }

  def sourceTypeAndSummaryTypeResponse(utr: SaUtr, taxYear: TaxYear,  sourceId: SourceId, summaryId: SummaryId) =
    sourceTypeAndSummaryTypeIdResponse(obj(), utr, taxYear, SourceTypes.SelfEmployments, sourceId, selfemployment.SummaryTypes.Incomes, summaryId)

  def sourceTypeAndPropertyTypeResponse(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, propertyType: YearPropertyType, sourceId: SourceId, jsValue: Option[JsValue]) = {
    val jsObj = jsValue.getOrElse(obj())
    val hal = halResource(jsObj, Set(HalLink("self", sourceTypeAndSummaryTypeHref(utr, taxYear, sourceType, sourceId, propertyType.name))))
    prettyPrint(hal.json)
  }

  def sourceTypeAndSummaryTypeIdResponse(jsValue: JsValue, utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryType: SummaryType, summaryId: SummaryId) = {
    val hal = halResource(jsValue, Set(HalLink("self", sourceTypeAndSummaryTypeIdHref(utr, taxYear, sourceType, sourceId, summaryType.name, summaryId))))
    prettyPrint(hal.json)
  }

  def sourceLinkResponse(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = {
    sourceModelResponse(obj(), utr, taxYear, sourceType, sourceId)
  }

  def sourceModelResponse(jsValue: JsValue, utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = {
    val hal = halResource(jsValue, sourceLinks(utr, taxYear, sourceType, sourceId))
    prettyPrint(hal.json)
  }

  def sourceTypeAndSummaryTypeIdListResponse(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId, summaryType: SummaryType, summaryId: SummaryId) = {
    val json = toJson(Seq(summaryId, summaryId, summaryId).map(id => halResource(summaryType.example(Some(summaryId)),
      Set(HalLink("self", sourceTypeAndSummaryTypeIdHref(utr, taxYear, sourceType, sourceId, summaryType.name, id))))))
    val hal = halResourceList(summaryType.name, json, sourceTypeAndSummaryTypeHref(utr, taxYear, sourceType, sourceId, summaryType.name))
    PCData(Json.prettyPrint(hal.json))
  }

  def sourceTypeIdListResponse(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId) = {
    val json = toJson(Seq(sourceId, sourceId, sourceId).map(id => halResource(sourceType.example(Some(sourceId)),
      Set(HalLink("self", sourceIdHref(utr, taxYear, sourceType, id))))))
    val hal = halResourceList(sourceType.name, json, sourceHref(utr, taxYear, sourceType))
    prettyPrint(hal.json)
  }

  def resolveTaxpayerResponse(utr: SaUtr) = {
    val hal = halResource(obj(), Set(HalLink("self-assessment", discoverTaxYearsHref(utr))))
    prettyPrint(hal.json)
  }

  def createLiabilityResponse(utr: SaUtr, taxYear: TaxYear) = {
    val hal = halResource(obj(), Set(HalLink("self", liabilityHref(utr, taxYear))))
    prettyPrint(hal.json)
  }

  def liabilityResponse(utr: SaUtr, taxYear: TaxYear) = {
    val hal = halResource(Json.toJson(Liability.example), Set(HalLink("self", liabilityHref(utr, taxYear))))
    prettyPrint(hal.json)
  }

  def discoverTaxYearsResponse(utr: SaUtr, taxYear: TaxYear) = {
    val hal = halResource(obj(), Set(HalLink("self", discoverTaxYearsHref(utr)), HalLink(taxYear.taxYear, discoverTaxYearHref(utr, taxYear))))
    prettyPrint(hal.json)
  }

  def discoverTaxYearResponse(utr: SaUtr, taxYear: TaxYear, jsValue: Option[JsValue] = None) = {
    val links = discoveryLinks(utr, taxYear)
    val hal = halResource(jsValue.getOrElse(obj()), links)
    prettyPrint(hal.json)
  }

  def discoveryLinks(utr: SaUtr, taxYear: TaxYear): Set[HalLink] = {
    val sourceLinks = enabledSourceTypes.map(sourceType => HalLink(sourceType.name, sourceHref(utr, taxYear, sourceType)))
    val links = sourceLinks + HalLink("liability", liabilityHref(utr, taxYear)) + HalLink("self", discoverTaxYearHref(utr, taxYear))
    links
  }

  def prettyPrint(jsValue: JsValue): PCData =
    PCData(Json.prettyPrint(jsValue))

}
