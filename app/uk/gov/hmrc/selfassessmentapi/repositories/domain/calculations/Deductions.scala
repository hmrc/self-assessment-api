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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations

import uk.gov.hmrc.selfassessmentapi.controllers.api.{SelfAssessment, AllowancesAndReliefs}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories._

object Deductions {

  def apply(selfAssessment: SelfAssessment) = AllowancesAndReliefs(incomeTaxRelief = Some(Deductions.IncomeTaxRelief(selfAssessment)),
    personalAllowance = Some(Deductions.PersonalAllowance(selfAssessment)),
    retirementAnnuityContract = Some(Deductions.RetirementAnnuityContract(selfAssessment)))

  object LossBroughtForward {
    def apply(selfEmployment: domain.SelfEmployment): BigDecimal = selfEmployment.adjustments.map(_.lossBroughtForward).getOrElse(BigDecimal(0))
  }

  object IncomeTaxRelief {

    def apply(ukPropertyTotalLBF: BigDecimal, selfEmploymentTotalLBF: BigDecimal, furnishedHolidayLettingTotalLBF: BigDecimal): BigDecimal =
      ukPropertyTotalLBF + selfEmploymentTotalLBF + furnishedHolidayLettingTotalLBF

    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(UKProperty.CappedTotalLossBroughtForward(selfAssessment),
      SelfEmployment.TotalLossBroughtForward(selfAssessment), FurnishedHolidayLetting.CappedTotalLossBroughtForward(selfAssessment))
  }

  object Total {
    def apply(incomeTaxRelief: BigDecimal, personalAllowance: BigDecimal, retirementAnnuityContract: BigDecimal): BigDecimal = {
      incomeTaxRelief + personalAllowance + retirementAnnuityContract
    }

    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(Deductions.IncomeTaxRelief(selfAssessment),
      Deductions.PersonalAllowance(selfAssessment), Deductions.RetirementAnnuityContract(selfAssessment))
  }

  object PersonalAllowance {
    private val standardAllowance = BigDecimal(11000)
    private val taperingThreshold = BigDecimal(100000)

    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(Totals.IncomeReceived(selfAssessment), Deductions.IncomeTaxRelief(selfAssessment),
      Deductions.PensionContribution(selfAssessment))

    def apply(totalIncomeReceived: BigDecimal, incomeTaxRelief: BigDecimal, pensionContribution: BigDecimal): BigDecimal = {
      RoundDownToEven(totalIncomeReceived - incomeTaxRelief - pensionContribution) match {
        case income if income <= taperingThreshold => standardAllowance
        case income if income > taperingThreshold => PositiveOrZero(standardAllowance - ((income - taperingThreshold) / 2))
      }
    }
  }

  object RetirementAnnuityContract {
    def apply(selfAssessment: SelfAssessment): BigDecimal =
      ValueOrZero(getPensionContribution(selfAssessment).map ( pensionContribution =>
        RoundUp(SumOptionals(pensionContribution.employerScheme, pensionContribution.overseasPension, pensionContribution.retirementAnnuity))
      ))
  }

  object PensionContribution {
    def apply(selfAssessment: SelfAssessment): BigDecimal =
      ValueOrZero(getPensionContribution(selfAssessment).map(pensionContribution =>
        SumOptionals(pensionContribution.employerScheme, pensionContribution.overseasPension, pensionContribution.retirementAnnuity,
          pensionContribution.ukRegisteredPension)
      ))
  }

  object TotalUkPensionContributions {
    def apply(selfAssessment: SelfAssessment): BigDecimal =
      ValueOrZero(getPensionContribution(selfAssessment).flatMap(_.ukRegisteredPension))
  }

  private def getPensionContribution(selfAssessment: SelfAssessment) = for {
    taxYearProps <- selfAssessment.taxYearProperties
    pensionContribution <- taxYearProps.pensionContributions
  } yield pensionContribution
}
