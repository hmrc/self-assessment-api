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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYearProperties
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.{PensionContribution, PensionSaving}

case class TaxYearPropertiesBuilder(objectID: BSONObjectID = BSONObjectID.generate) {
  def create(): TaxYearProperties = taxYearProperties

  private var taxYearProperties: TaxYearProperties =
    TaxYearProperties(pensionContributions = Some(PensionContribution()))

  def ukRegisteredPension(amount: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(
      pensionContributions = taxYearProperties.pensionContributions.map(_.copy(ukRegisteredPension = Some(amount))))
    this
  }

  def retirementAnnuityContract(amount: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(
      pensionContributions = taxYearProperties.pensionContributions.map(_.copy(retirementAnnuity = Some(amount))))
    this
  }

  def employerScheme(amount: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(
      pensionContributions = taxYearProperties.pensionContributions.map(_.copy(employerScheme = Some(amount))))
    this
  }

  def overseasPension(amount: BigDecimal) = {
    taxYearProperties = taxYearProperties.copy(
      pensionContributions = taxYearProperties.pensionContributions.map(_.copy(overseasPension = Some(amount))))
    this
  }

  def pensionSavings(excessOfAnnualAllowance: BigDecimal = 0, taxPaidByPensionScheme: BigDecimal = 0) = {
    taxYearProperties = taxYearProperties.copy(pensionContributions =
      taxYearProperties.pensionContributions.map(_.copy(pensionSavings =
        Some(PensionSaving(excessOfAnnualAllowance = Some(excessOfAnnualAllowance), taxPaidByPensionScheme = Some(taxPaidByPensionScheme))))))
    this
  }
}
