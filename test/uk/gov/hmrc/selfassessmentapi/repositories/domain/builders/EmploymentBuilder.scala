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
import uk.gov.hmrc.selfassessmentapi.TestUtils._
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.BenefitType.{Accommodation, CompanyVehicle, ExpensesPayments, PrivateInsurance}
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.ExpenseType.{ProfessionalFees, TravelAndSubsistence}
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.IncomeType.{Other, Salary}
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.{BenefitType, ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

case class EmploymentBuilder(objectID: BSONObjectID = BSONObjectID.generate) {

  private var anEmployment: Employment = Employment(objectID, objectID.stringify, generateSaUtr(), taxYear, now, now)

  private def withIncomes(incomes: (IncomeType.IncomeType, BigDecimal)*) = {
    anEmployment = anEmployment.copy(incomes = anEmployment.incomes ++ incomes.map (
      income => EmploymentIncomeSummary(objectID.stringify, income._1, income._2)))
    this
  }

  def withSalary(salary: BigDecimal*) = {
    withIncomes(salary.map((Salary, _)):_*)
  }

  def withOtherIncome(salary: BigDecimal*) = {
    withIncomes(salary.map((Other, _)):_*)
  }

  private def withExpenses(expenses: (ExpenseType.ExpenseType, BigDecimal)*) = {
    anEmployment = anEmployment.copy(expenses = anEmployment.expenses ++ expenses.map (
      expense => EmploymentExpenseSummary(objectID.stringify, expense._1, expense._2)))
    this
  }

  def withTravelAndSubsistenceExpense(expenses: BigDecimal*) = {
    withExpenses(expenses.map((TravelAndSubsistence,_)):_*)
  }

  def withProfessionalFeesExpense(expenses: BigDecimal*) = {
    withExpenses(expenses.map((ProfessionalFees,_)):_*)
  }

  private def withBenefits(benefits: (BenefitType.BenefitType, BigDecimal)*) = {
    anEmployment = anEmployment.copy(benefits = anEmployment.benefits ++ benefits.map (
      benefit => EmploymentBenefitSummary(objectID.stringify, benefit._1, benefit._2)))
    this
  }

  def withAccommodationBenefit(accommodations : BigDecimal*) = {
    withBenefits(accommodations.map((Accommodation, _)):_*)
  }

  def withOtherBenefit(others : BigDecimal*) = {
    withBenefits(others.map((BenefitType.Other, _)):_*)
  }

  def withCompanyVehicleBenefit(companyVehicles : BigDecimal*) = {
    withBenefits(companyVehicles.map((CompanyVehicle, _)):_*)
  }

  def withExpensesPaymentsBenefit(expensesPayments : BigDecimal*) = {
    withBenefits(expensesPayments.map((ExpensesPayments, _)):_*)
  }

  def withPrivateInsuranceBenefit(privateInsurance : BigDecimal*) = {
    withBenefits(privateInsurance.map((PrivateInsurance, _)):_*)
  }

  def withUkTaxPaid(taxPaid: BigDecimal) = {
    anEmployment = anEmployment.copy(ukTaxPaid =
      Seq(EmploymentUkTaxPaidSummary(objectID.stringify, amount = taxPaid)))
    this
  }

  def create() = anEmployment
}
