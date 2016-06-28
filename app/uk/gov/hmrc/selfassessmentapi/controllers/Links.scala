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

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain._

trait Links {

  val context: String

  private def createLink(endpointUrl: String) = s"/$context$endpointUrl"

  def discoverTaxYearsHref(utr: SaUtr): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.TaxYearsDiscoveryController.discoverTaxYears(utr).url)

  def discoverTaxYearHref(utr: SaUtr, taxYear: TaxYear): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.TaxYearDiscoveryController.discoverTaxYear(utr, taxYear).url)

  def liabilityHref(utr: SaUtr, taxYear: TaxYear, liabilityId: String): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.LiabilityController.retrieveLiability(utr, taxYear, liabilityId).url)

  def liabilitiesHref(utr: SaUtr, taxYear: TaxYear): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.LiabilityController.find(utr, taxYear).url)

  def sourceIdHref(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, seId: SourceId): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SourceController.read(utr, taxYear, sourceType, seId).url)

  def sourceHref(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SourceController.list(utr, taxYear, sourceType).url)

  def sourceTypeAndSummaryTypeHref(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, seId: SourceId, summaryTypeName: String): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SummaryController.list(utr, taxYear, sourceType, seId, summaryTypeName).url)

  def sourceTypeAndSummaryTypeIdHref(utr: SaUtr, taxYear: TaxYear, sourceType: SourceType, seId: SourceId, summaryTypeName: String, id: String): String =
    createLink(uk.gov.hmrc.selfassessmentapi.controllers.live.routes.SummaryController.read(utr, taxYear, sourceType, seId, summaryTypeName, id).url)
}
