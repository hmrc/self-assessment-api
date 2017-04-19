/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.properties

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models._

object FHL {

  final case class Properties(from: LocalDate, to: LocalDate, financials: Financials) extends Period

  object Properties extends PeriodValidator[Properties] {

    implicit val writes = new Writes[Properties] {
      override def writes(o: Properties): JsValue = {
        Json.obj(
          "from" -> o.from,
          "to" -> o.to,
          "incomes" -> o.financials.incomes,
          "expenses" -> o.financials.expenses
        )
      }
    }

    implicit val reads: Reads[Properties] = (
      (__ \ "from").read[LocalDate] and
        (__ \ "to").read[LocalDate] and
        (__ \ "incomes").readNullable[Incomes] and
        (__ \ "expenses").readNullable[Expenses]
    )((from, to, incomes, expenses) => {
      Properties(from, to, Financials(incomes, expenses))
    }).filter(ValidationError("the period 'from' date should come before the 'to' date", ErrorCode.INVALID_PERIOD))(
      periodDateValidator)

  }

  case class Income(amount: Amount)

  object Income {
    implicit val reads: Reads[Income] = (__ \ "amount").read[Amount](nonNegativeAmountValidator).map(Income(_))

    implicit val writes: Writes[Income] = Json.writes[Income]
  }

  case class Incomes(rentIncome: Option[Income] = None)

  object Incomes {
    implicit val format: Format[Incomes] = Json.format[Incomes]
  }

  case class Expense(amount: Amount)

  object Expense {
    implicit val reads: Reads[Expense] = (__ \ "amount").read[Amount](nonNegativeAmountValidator).map(Expense(_))

    implicit val writes: Writes[Expense] = Json.writes[Expense]
  }

  case class Expenses(premisesRunningCosts: Option[Expense] = None,
                      repairsAndMaintenance: Option[Expense] = None,
                      financialCosts: Option[Expense] = None,
                      professionalFees: Option[Expense] = None,
                      other: Option[Expense] = None)

  object Expenses {
    implicit val format: Format[Expenses] = Json.format[Expenses]
  }

  final case class Financials(incomes: Option[Incomes] = None, expenses: Option[Expenses] = None)
      extends models.Financials

  object Financials {
    implicit val format: Format[Financials] =
      Json.format[Financials]

  }
}

object Other {
  final case class Properties(from: LocalDate, to: LocalDate, financials: Financials) extends Period

  object Properties extends PeriodValidator[Properties] {

    implicit val writes = new Writes[Properties] {
      override def writes(o: Properties): JsValue = {
        Json.obj(
          "from" -> o.from,
          "to" -> o.to,
          "incomes" -> o.financials.incomes,
          "expenses" -> o.financials.expenses
        )
      }
    }

    implicit val reads: Reads[Properties] = (
      (__ \ "from").read[LocalDate] and
        (__ \ "to").read[LocalDate] and
        (__ \ "incomes").readNullable[Incomes] and
        (__ \ "expenses").readNullable[Expenses]
    )((from, to, incomes, expenses) => {
      Properties(from, to, Financials(incomes, expenses))
    }).filter(ValidationError("the period 'from' date should come before the 'to' date", ErrorCode.INVALID_PERIOD))(
      periodDateValidator)
  }

  case class Income(amount: Amount, taxDeducted: Option[Amount])

  object Income {
    implicit val reads: Reads[Income] = (
      (__ \ "amount").read[Amount](nonNegativeAmountValidator) and
        (__ \ "taxDeducted").readNullable[Amount](nonNegativeAmountValidator)
    )(Income.apply _)

    implicit val writes: Writes[Income] = Json.writes[Income]
  }

  case class Incomes(rentIncome: Option[Income] = None,
                     premiumsOfLeaseGrant: Option[Income] = None,
                     reversePremiums: Option[Income] = None)

  object Incomes {

    implicit val format: Format[Incomes] =
      Json.format[Incomes]
  }

  case class Expense(amount: Amount)

  object Expense {
    implicit val reads: Reads[Expense] = (__ \ "amount").read[Amount](nonNegativeAmountValidator).map(Expense(_))

    implicit val writes: Writes[Expense] = Json.writes[Expense]
  }

  case class Expenses(premisesRunningCosts: Option[Expense] = None,
                      repairsAndMaintenance: Option[Expense] = None,
                      financialCosts: Option[Expense] = None,
                      professionalFees: Option[Expense] = None,
                      costOfServices: Option[Expense] = None,
                      other: Option[Expense] = None)

  object Expenses {
    implicit val format: Format[Expenses] = Json.format[Expenses]
  }

  final case class Financials(incomes: Option[Incomes] = None, expenses: Option[Expenses] = None)
      extends models.Financials

  object Financials {

    implicit val format: Format[Financials] =
      Json.format[Financials]
  }

}
