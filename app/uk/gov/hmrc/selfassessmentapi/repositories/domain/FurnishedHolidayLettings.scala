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
import uk.gov.hmrc.domain._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}

case class FurnishedHolidayLettingsIncomeSummary(summaryId: SummaryId,
                                                 amount: BigDecimal) extends Summary with AmountHolder {
  val arrayName = FurnishedHolidayLettingsIncomeSummary.arrayName

  def toIncome: Income =
    Income(id = Some(summaryId),
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object FurnishedHolidayLettingsIncomeSummary {

  val arrayName = "incomes"

  implicit val format = Json.format[FurnishedHolidayLettingsIncomeSummary]

  def toMongoSummary(income: Income, id: Option[SummaryId] = None): FurnishedHolidayLettingsIncomeSummary = {
    FurnishedHolidayLettingsIncomeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = income.amount
    )
  }
}

case class FurnishedHolidayLettingsExpenseSummary(summaryId: SummaryId,
                                                  `type`: ExpenseType,
                                                  amount: BigDecimal) extends Summary with AmountHolder {
  val arrayName = FurnishedHolidayLettingsExpenseSummary.arrayName

  def toExpense: Expense =
    Expense(id = Some(summaryId),
      `type` = `type`,
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object FurnishedHolidayLettingsExpenseSummary {

  val arrayName = "expenses"

  implicit val format = Json.format[FurnishedHolidayLettingsExpenseSummary]

  def toMongoSummary(expense: Expense, id: Option[SummaryId] = None): FurnishedHolidayLettingsExpenseSummary = {
    FurnishedHolidayLettingsExpenseSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = expense.`type`,
      amount = expense.amount
    )
  }
}

case class FurnishedHolidayLettingsBalancingChargeSummary(summaryId: SummaryId,
                                                          amount: BigDecimal) extends Summary with AmountHolder {
  val arrayName = FurnishedHolidayLettingsBalancingChargeSummary.arrayName

  def toBalancingCharge =
    BalancingCharge(id = Some(summaryId),
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object FurnishedHolidayLettingsBalancingChargeSummary {

  val arrayName = "balancingCharges"

  implicit val format = Json.format[FurnishedHolidayLettingsBalancingChargeSummary]

  def toMongoSummary(balancingCharge: BalancingCharge, id: Option[SummaryId] = None): FurnishedHolidayLettingsBalancingChargeSummary = {
    FurnishedHolidayLettingsBalancingChargeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = balancingCharge.amount
    )
  }
}

case class FurnishedHolidayLettingsPrivateUseAdjustmentSummary(summaryId: SummaryId, amount: BigDecimal) extends Summary
  with AmountHolder {

  val arrayName = FurnishedHolidayLettingsPrivateUseAdjustmentSummary.arrayName

  def toPrivateUseAdjustment = PrivateUseAdjustment(id = Some(summaryId), amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object FurnishedHolidayLettingsPrivateUseAdjustmentSummary {

  val arrayName = "privateUseAdjustment"

  implicit val format = Json.format[FurnishedHolidayLettingsPrivateUseAdjustmentSummary]

  def toMongoSummary(privateUseAdjustment: PrivateUseAdjustment, id: Option[SummaryId] = None): FurnishedHolidayLettingsPrivateUseAdjustmentSummary = {
    FurnishedHolidayLettingsPrivateUseAdjustmentSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = privateUseAdjustment.amount
    )
  }
}

case class FurnishedHolidayLettings(id: BSONObjectID,
                                    sourceId: SourceId,
                                    nino: Nino,
                                    taxYear: TaxYear,
                                    lastModifiedDateTime: DateTime,
                                    createdDateTime: DateTime,
                                    propertyLocation: PropertyLocationType,
                                    allowances: Option[Allowances] = None,
                                    adjustments: Option[Adjustments] = None,
                                    incomes: Seq[FurnishedHolidayLettingsIncomeSummary] = Nil,
                                    expenses: Seq[FurnishedHolidayLettingsExpenseSummary] = Nil,
                                    balancingCharges: Seq[FurnishedHolidayLettingsBalancingChargeSummary] = Nil,
                                    privateUseAdjustment: Seq[FurnishedHolidayLettingsPrivateUseAdjustmentSummary] = Nil) extends SourceMetadata {


  def capitalAllowance: BigDecimal = allowances.flatMap(_.capitalAllowance).getOrElse(0)


  def toFurnishedHolidayLettings = FurnishedHolidayLetting(
    id = Some(sourceId),
    propertyLocation = propertyLocation,
    allowances = allowances,
    adjustments = adjustments)
}

object FurnishedHolidayLettings {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[FurnishedHolidayLettings], Json.writes[FurnishedHolidayLettings])
  })

  def create(nino: Nino, taxYear: TaxYear, fhl: FurnishedHolidayLetting): FurnishedHolidayLettings = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)
    FurnishedHolidayLettings(
      id = id,
      sourceId = id.stringify,
      nino = nino,
      taxYear = taxYear,
      lastModifiedDateTime = now,
      createdDateTime = now,
      propertyLocation = fhl.propertyLocation,
      allowances = fhl.allowances,
      adjustments = fhl.adjustments)
  }
}
