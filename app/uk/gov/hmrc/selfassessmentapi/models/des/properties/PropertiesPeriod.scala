/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.des.properties

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.des.properties.Common.Income
import uk.gov.hmrc.selfassessmentapi.models.properties

object Common {
  case class Income(amount: BigDecimal, taxDeducted: Option[BigDecimal] = None)

  object Income {
    implicit val format: OFormat[Income] = Json.format[Income]

    def fromIncome(o: models.Income): Income =
      Income(amount = o.amount, taxDeducted = o.taxDeducted)
  }

}

object FHL {

  case class Incomes(rentIncome: Option[Income] = None)

  object Incomes {
    implicit val format: OFormat[Incomes] = Json.format[Incomes]

    def from(o: properties.FHL.Incomes): Incomes =
      Incomes(rentIncome = o.rentIncome.map(Income.fromIncome))
  }

  case class Deductions(premisesRunningCosts: Option[BigDecimal] = None,
                        repairsAndMaintenance: Option[BigDecimal] = None,
                        financialCosts: Option[BigDecimal] = None,
                        professionalFees: Option[BigDecimal] = None,
                        costOfServices: Option[BigDecimal] = None,
                        other: Option[BigDecimal] = None,
                        consolidatedExpenses: Option[BigDecimal] = None)

  object Deductions {
    implicit val format: OFormat[Deductions] = Json.format[Deductions]

    def from(o: properties.FHL.Expenses): Deductions =
      Deductions(premisesRunningCosts = o.premisesRunningCosts.map(_.amount),
                 repairsAndMaintenance = o.repairsAndMaintenance.map(_.amount),
                 financialCosts = o.financialCosts.map(_.amount),
                 professionalFees = o.professionalFees.map(_.amount),
                 costOfServices = o.costOfServices.map(_.amount),
                 consolidatedExpenses = o.consolidatedExpenses.map(_.amount),
                 other = o.other.map(_.amount))
  }

  case class Financials(incomes: Option[Incomes] = None, deductions: Option[Deductions] = None)

  object Financials {
    implicit val format: OFormat[Financials] = Json.format[Financials]

    def from(financials: Option[properties.FHL.Financials]): Option[Financials] =
      financials.flatMap { f =>
        (f.incomes, f.expenses) match {
          case (None, None) => None
          case (incomes, expenses) => Some(Financials(incomes = incomes.map(Incomes.from),
            deductions = expenses.map(Deductions.from)
          ))
        }
      }
  }

  case class Properties(transactionReference: Option[String], from: String, to: String, financials: Option[Financials]) extends Period

  object Properties {
    implicit val format: OFormat[Properties] = Json.format[Properties]

    def from(o: properties.FHL.Properties): Properties =
      Properties(transactionReference = None, from = o.from.toString, to = o.to.toString, financials = Financials.from(o.financials))
  }

}

object Other {

  case class Incomes(rentIncome: Option[Income] = None,
                     premiumsOfLeaseGrant: Option[BigDecimal] = None,
                     reversePremiums: Option[BigDecimal] = None,
                     otherPropertyIncome: Option[BigDecimal] = None)

  object Incomes {
    implicit val format: OFormat[Incomes] = Json.format[Incomes]

    def from(o: properties.Other.Incomes): Incomes =
      Incomes(rentIncome = o.rentIncome.map(Income.fromIncome),
              premiumsOfLeaseGrant = o.premiumsOfLeaseGrant.map(_.amount),
              reversePremiums = o.reversePremiums.map(_.amount),
              otherPropertyIncome = o.otherPropertyIncome.map(_.amount))
  }

  case class Deductions(premisesRunningCosts: Option[BigDecimal] = None,
                        repairsAndMaintenance: Option[BigDecimal] = None,
                        financialCosts: Option[BigDecimal] = None,
                        professionalFees: Option[BigDecimal] = None,
                        costOfServices: Option[BigDecimal] = None,
                        other: Option[BigDecimal] = None,
                        consolidatedExpenses: Option[BigDecimal] = None,
                        residentialFinancialCost: Option[BigDecimal] = None)

  object Deductions {
    implicit val format: OFormat[Deductions] = Json.format[Deductions]

    def from(o: properties.Other.Expenses): Deductions =
      Deductions(premisesRunningCosts = o.premisesRunningCosts.map(_.amount),
                 repairsAndMaintenance = o.repairsAndMaintenance.map(_.amount),
                 financialCosts = o.financialCosts.map(_.amount),
                 professionalFees = o.professionalFees.map(_.amount),
                 costOfServices = o.costOfServices.map(_.amount),
                 consolidatedExpenses = o.consolidatedExpenses.map(_.amount),
                 residentialFinancialCost = o.residentialFinancialCost.map(_.amount),
                 other = o.other.map(_.amount))
  }

  case class Financials(incomes: Option[Incomes] = None, deductions: Option[Deductions] = None)

  object Financials {
    implicit val format: OFormat[Financials] = Json.format[Financials]

    def from(financials: Option[properties.Other.Financials]): Option[Financials] =
    financials.flatMap { f =>
      (f.incomes, f.expenses) match {
        case (None, None) => None
        case (incomes, expenses) => Some(Financials(incomes = incomes.map(Incomes.from),
          deductions = expenses.map(Deductions.from)
        ))
      }
    }
  }

  case class Properties(transactionReference: Option[String], from: String, to: String, financials: Option[Financials]) extends Period

  object Properties {

    implicit val format: OFormat[Properties] = Json.format[Properties]

    def from(o: properties.Other.Properties): Properties =
      Properties(transactionReference = None, from = o.from.toString, to = o.to.toString, financials = Financials.from(o.financials))
  }

}
