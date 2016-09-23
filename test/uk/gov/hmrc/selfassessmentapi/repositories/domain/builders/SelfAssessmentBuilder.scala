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

import uk.gov.hmrc.selfassessmentapi.controllers.api.SelfAssessment

class SelfAssessmentBuilder {

  private var selfAssessment = SelfAssessment()

  def withSelfEmployments(selfEmployments: SelfEmploymentBuilder*) = {
    selfAssessment = selfAssessment.copy(selfEmployments = selfEmployments.map(_.create()))
    this
  }

  def withEmployments(employments: EmploymentBuilder*) = {
    selfAssessment = selfAssessment.copy(employments = employments.map(_.create()))
    this
  }

  def withFurnishedHolidayLettings(furnishedHolidayLettings: FurnishedHolidayLettingBuilder*) = {
    selfAssessment = selfAssessment.copy(furnishedHolidayLettings = furnishedHolidayLettings.map(_.create()))
    this
  }

  def withUkProperties(ukProperties: UKPropertyBuilder*) = {
    selfAssessment = selfAssessment.copy(ukProperties = ukProperties.map(_.create()))
    this
  }

  def withUnearnedIncomes(unearnedIncomes: UnearnedIncomeBuilder*) = {
    selfAssessment = selfAssessment.copy(unearnedIncomes = unearnedIncomes.map(_.create()))
    this
  }

  def withTaxYearProperties(taxYearProperties: TaxYearPropertiesBuilder) = {
    selfAssessment = selfAssessment.copy(taxYearProperties = Some(taxYearProperties.create()))
    this
  }

  def create() = selfAssessment
}

object SelfAssessmentBuilder {
  def apply() = new SelfAssessmentBuilder()
}
