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

package uk.gov.hmrc.selfassessmentapi.repositories.domain

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.{BSONDocument, BSONDouble, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.BenefitType.BenefitType
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment._
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.IncomeType.IncomeType

case class EmploymentIncomeSummary(summaryId: SummaryId, `type`: IncomeType, amount: BigDecimal)
    extends Summary with AmountHolder {
  val arrayName = EmploymentIncomeSummary.arrayName

  def toIncome: Income =
    Income(id = Some(summaryId), `type` = `type`, amount = amount)

  def toBsonDocument = BSONDocument(
      "summaryId" -> summaryId,
      "amount" -> BSONDouble(amount.doubleValue()),
      "type" -> BSONString(`type`.toString)
  )
}

object EmploymentIncomeSummary {

  val arrayName = "incomes"

  implicit val format = Json.format[EmploymentIncomeSummary]

  def toMongoSummary(income: Income, id: Option[SummaryId] = None): EmploymentIncomeSummary = {
    EmploymentIncomeSummary(
        summaryId = id.getOrElse(BSONObjectID.generate.stringify),
        `type` = income.`type`,
        amount = income.amount
    )
  }
}

case class EmploymentExpenseSummary(summaryId: SummaryId, `type`: ExpenseType, amount: BigDecimal)
    extends Summary with AmountHolder {
  val arrayName = EmploymentExpenseSummary.arrayName

  def toExpense: Expense =
    Expense(id = Some(summaryId), `type` = `type`, amount = amount)

  def toBsonDocument = BSONDocument(
      "summaryId" -> summaryId,
      "amount" -> BSONDouble(amount.doubleValue()),
      "type" -> BSONString(`type`.toString)
  )
}

object EmploymentExpenseSummary {

  val arrayName = "expenses"

  implicit val format = Json.format[EmploymentExpenseSummary]

  def toMongoSummary(expense: Expense, id: Option[SummaryId] = None): EmploymentExpenseSummary = {
    EmploymentExpenseSummary(
        summaryId = id.getOrElse(BSONObjectID.generate.stringify),
        `type` = expense.`type`,
        amount = expense.amount
    )
  }
}

case class EmploymentBenefitSummary(summaryId: SummaryId, `type`: BenefitType, amount: BigDecimal)
    extends Summary with AmountHolder {
  val arrayName = EmploymentBenefitSummary.arrayName

  def toBenefit: Benefit =
    Benefit(id = Some(summaryId), `type` = `type`, amount = amount)

  def toBsonDocument = BSONDocument(
      "summaryId" -> summaryId,
      "amount" -> BSONDouble(amount.doubleValue()),
      "type" -> BSONString(`type`.toString)
  )
}

object EmploymentBenefitSummary {

  val arrayName = "benefits"

  implicit val format = Json.format[EmploymentBenefitSummary]

  def toMongoSummary(benefit: Benefit, id: Option[SummaryId] = None): EmploymentBenefitSummary = {
    EmploymentBenefitSummary(
        summaryId = id.getOrElse(BSONObjectID.generate.stringify),
        `type` = benefit.`type`,
        amount = benefit.amount
    )
  }
}

case class EmploymentUkTaxPaidSummary(summaryId: SummaryId, amount: BigDecimal) extends Summary {
  val arrayName = EmploymentUkTaxPaidSummary.arrayName

  def toUkTaxPaid: UkTaxPaid =
    UkTaxPaid(id = Some(summaryId), amount = amount)

  def toBsonDocument = BSONDocument(
      "summaryId" -> summaryId,
      "amount" -> BSONDouble(amount.doubleValue())
  )
}

object EmploymentUkTaxPaidSummary {

  val arrayName = "ukTaxPaid"

  implicit val format = Json.format[EmploymentUkTaxPaidSummary]

  def toMongoSummary(uKTaxPaid: UkTaxPaid, id: Option[SummaryId] = None): EmploymentUkTaxPaidSummary = {
    EmploymentUkTaxPaidSummary(
        summaryId = id.getOrElse(BSONObjectID.generate.stringify),
        amount = uKTaxPaid.amount
    )
  }
}

case class Employment(id: BSONObjectID,
                      sourceId: SourceId,
                      saUtr: SaUtr,
                      taxYear: TaxYear,
                      lastModifiedDateTime: DateTime,
                      createdDateTime: DateTime,
                      incomes: Seq[EmploymentIncomeSummary] = Nil,
                      expenses: Seq[EmploymentExpenseSummary] = Nil,
                      benefits: Seq[EmploymentBenefitSummary] = Nil,
                      ukTaxPaid: Seq[EmploymentUkTaxPaidSummary] = Nil)
    extends SourceMetadata {

  def toEmployment = employment.Employment(id = Some(sourceId))
}

object Employment {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[Employment], Json.writes[Employment])
  })

  def create(saUtr: SaUtr, taxYear: TaxYear, employment: api.employment.Employment): Employment = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)
    Employment(id = id,
                    sourceId = id.stringify,
                    saUtr = saUtr,
                    taxYear = taxYear,
                    lastModifiedDateTime = now,
                    createdDateTime = now)
  }
}
