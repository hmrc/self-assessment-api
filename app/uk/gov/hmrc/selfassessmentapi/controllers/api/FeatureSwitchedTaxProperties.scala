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

package uk.gov.hmrc.selfassessmentapi.controllers.api

import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.{BlindPerson, BlindPersons}
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.{CharitableGiving, CharitableGivings}
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.{ChildBenefit, ChildBenefits}
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.{PensionContribution, PensionContributions}
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.{StudentLoan, StudentLoans}
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.{TaxRefundedOrSetOff, TaxRefundedOrSetOffs}

object FeatureSwitchedTaxProperties {

  private val featureSwitch = FeatureSwitch(AppContext.featureSwitch)

  private val propertyTypes =
    Seq(PensionContributions, CharitableGivings, BlindPersons, StudentLoans, TaxRefundedOrSetOffs, ChildBenefits)

  def atLeastOnePropertyIsEnabled: Boolean = propertyTypes.exists(featureSwitch.isEnabled)

  def apply() =
    TaxYearProperties(
      pensionContributions = if(featureSwitch.isEnabled(PensionContributions)) Some(PensionContribution.example()) else None,
      charitableGivings = if(featureSwitch.isEnabled(CharitableGivings)) Some(CharitableGiving.example()) else None,
      blindPerson = if(featureSwitch.isEnabled(BlindPersons)) Some(BlindPerson.example()) else None,
      studentLoan = if(featureSwitch.isEnabled(StudentLoans)) Some(StudentLoan.example()) else None,
      taxRefundedOrSetOff = if(featureSwitch.isEnabled(TaxRefundedOrSetOffs)) Some(TaxRefundedOrSetOff.example()) else None,
      childBenefit = if(featureSwitch.isEnabled(ChildBenefits)) Some(ChildBenefit.example()) else None
    )
}
