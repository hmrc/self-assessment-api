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

import play.api.hal.{Hal, HalLink, HalResource}
import play.api.libs.json.{JsObject, JsValue}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureConfig}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.SourceController._

trait HalSupport {

  def halResource(jsValue: JsValue, links: Set[HalLink]): HalResource = {
    val halState = Hal.state(jsValue)
    links.foldLeft(halState)((res, link) => res ++ link)
  }

  def halResourceList(name: String, value: JsValue, self: String) = {
    halResource(
      JsObject(
        Seq(
          "_embedded" -> JsObject(
            Seq(name -> value))
        )
      ),
      Set(HalLink("self", self)))
  }

  private def allSourceLinks(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, seId: SourceId): Set[HalLink] = {
      sourceType.summaryTypes.map { summaryType =>
        HalLink(summaryType.name, sourceTypeAndSummaryTypeHref(utr, taxYear, sourceType, seId, summaryType.name))
      } + HalLink("self", sourceIdHref(utr, taxYear, sourceType, seId))
  }

  def sourceLinks(saUtr: SaUtr, taxYear: TaxYear, sourceType: SourceType, sourceId: SourceId): Set[HalLink] = {
    val allLinks = allSourceLinks(saUtr, taxYear, sourceType, sourceId)

    AppContext.featureSwitch.map(FeatureConfig) match {
      case Some(fc) => allLinks.filter { halLink =>
        fc.isSummaryEnabled(sourceType.name, halLink.rel)
      }
      case None => allLinks
    }
  }
}
