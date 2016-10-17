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

package uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._


case class PensionSaving(excessOfAnnualAllowance: Option[BigDecimal] = None, taxPaidByPensionScheme: Option[BigDecimal] = None)

object PensionSaving extends JsonMarshaller[PensionSaving] {
  override implicit val writes = Json.writes[PensionSaving]

  override implicit val reads = (
    (__ \ "excessOfAnnualAllowance").readNullable[BigDecimal](positiveAmountValidator("excessOfAnnualAllowance")) and
      (__ \ "taxPaidByPensionScheme").readNullable[BigDecimal](positiveAmountValidator("taxPaidByPensionScheme"))
    ) (PensionSaving.apply _)
    .filter(ValidationError("taxPaidByPensionScheme can not exist when there is no excessOfAnnualAllowance", ErrorCode.UNDEFINED_REQUIRED_ELEMENT))
  { savings => if (savings.taxPaidByPensionScheme.isDefined) savings.excessOfAnnualAllowance.isDefined else true }
    .filter(ValidationError("taxPaidByPensionScheme must not exceed the excessOfAnnualAllowance", ErrorCode.MAXIMUM_AMOUNT_EXCEEDED))
  { savings => savings.taxPaidByPensionScheme.forall(_ <= savings.excessOfAnnualAllowance.getOrElse(0)) }

  override def example(id: Option[String]): PensionSaving = PensionSaving(Some(200.00), Some(123.23))
}

case class PensionContribution(ukRegisteredPension: Option[BigDecimal] = None,
                               retirementAnnuity: Option[BigDecimal] = None,
                               employerScheme: Option[BigDecimal] = None,
                               overseasPension: Option[BigDecimal] = None,
                               pensionSavings: Option[PensionSaving] = None) {

  def retirementAnnuityContract: BigDecimal = {
    Sum(retirementAnnuity, employerScheme, overseasPension)
  }
}

object PensionContribution extends JsonMarshaller[PensionContribution] {

  override implicit val writes = Json.writes[PensionContribution]

  override implicit val reads = (
    (__ \ "ukRegisteredPension").readNullable[BigDecimal](positiveAmountValidator("ukRegisteredPension")) and
      (__ \ "retirementAnnuity").readNullable[BigDecimal](positiveAmountValidator("retirementAnnuity")) and
      (__ \ "employerScheme").readNullable[BigDecimal](positiveAmountValidator("employerScheme")) and
      (__ \ "overseasPension").readNullable[BigDecimal](positiveAmountValidator("overseasPension")) and
      (__ \ "pensionSavings").readNullable[PensionSaving]
    ) (PensionContribution.apply _)
      .filter(ValidationError("pensionSavings may only exist if there is at least one pension contribution", ErrorCode.UNDEFINED_REQUIRED_ELEMENT))
    {atLeastOneContributionDefined}
      .filter(ValidationError("excessOfAnnualAllowance may not exceed the sum of all pension contributions", ErrorCode.MAXIMUM_AMOUNT_EXCEEDED))
    {excessDoesNotExceedSumOfAllContributions}

  private def atLeastOneContributionDefined(contribution: PensionContribution): Boolean = {
    contribution.ukRegisteredPension.isDefined || contribution.retirementAnnuity.isDefined ||
      contribution.employerScheme.isDefined || contribution.overseasPension.isDefined
  }

  private def excessDoesNotExceedSumOfAllContributions(contribution: PensionContribution): Boolean = {
    contribution.pensionSavings.forall(_.excessOfAnnualAllowance.forall(_ <= sumOfAllContributions(contribution)))
  }

  private def sumOfAllContributions(contribution: PensionContribution): BigDecimal = {
    Sum(contribution.ukRegisteredPension, contribution.retirementAnnuity,
      contribution.employerScheme, contribution.overseasPension)
  }

  override def example(id: Option[String] = None) =
    PensionContribution(
      ukRegisteredPension = Some(1000.45),
      retirementAnnuity = Some(1000.00),
      employerScheme = Some(12000.05),
      overseasPension = Some(1234.43),
      pensionSavings = Some(PensionSaving.example()))
}
