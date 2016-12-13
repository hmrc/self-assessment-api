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
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{Adjustments, Allowances}

case class UKPropertiesIncomeSummary(summaryId: SummaryId,
                                     `type`: IncomeType,
                                     amount: BigDecimal) extends Summary with AmountHolder {

  val arrayName = UKPropertiesIncomeSummary.arrayName

  def toIncome: Income = Income(id = Some(summaryId), `type` = `type`, amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object UKPropertiesIncomeSummary {

  val arrayName = "incomes"

  implicit val format = Json.format[UKPropertiesIncomeSummary]

  def toMongoSummary(income: Income, id: Option[SummaryId] = None): UKPropertiesIncomeSummary = {
    UKPropertiesIncomeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = income.`type`,
      amount = income.amount
    )
  }
}

case class UKPropertiesExpenseSummary(summaryId: SummaryId,
                                      `type`: ExpenseType,
                                      amount: BigDecimal) extends Summary with AmountHolder {
  val arrayName = UKPropertiesExpenseSummary.arrayName

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

object UKPropertiesExpenseSummary {

  val arrayName = "expenses"

  implicit val format = Json.format[UKPropertiesExpenseSummary]

  def toMongoSummary(expense: Expense, id: Option[SummaryId] = None): UKPropertiesExpenseSummary = {
    UKPropertiesExpenseSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = expense.`type`,
      amount = expense.amount
    )
  }
}

case class UKPropertiesBalancingChargeSummary(summaryId: SummaryId,
                                              amount: BigDecimal) extends Summary with AmountHolder {
  val arrayName = UKPropertiesBalancingChargeSummary.arrayName

  def toBalancingCharge =
    BalancingCharge(id = Some(summaryId),
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object UKPropertiesBalancingChargeSummary {

  val arrayName = "balancingCharges"

  implicit val format = Json.format[UKPropertiesBalancingChargeSummary]

  def toMongoSummary(balancingCharge: BalancingCharge, id: Option[SummaryId] = None): UKPropertiesBalancingChargeSummary = {
    UKPropertiesBalancingChargeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = balancingCharge.amount
    )
  }
}

case class UKPropertiesPrivateUseAdjustmentSummary(summaryId: SummaryId, amount: BigDecimal) extends Summary with AmountHolder {

  val arrayName = UKPropertiesPrivateUseAdjustmentSummary.arrayName

  def toGoodsAndServicesOwnUse = PrivateUseAdjustment(id = Some(summaryId), amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object UKPropertiesPrivateUseAdjustmentSummary {

  val arrayName = "privateUseAdjustment"

  implicit val format = Json.format[UKPropertiesPrivateUseAdjustmentSummary]

  def toMongoSummary(privateUseAdjustment: PrivateUseAdjustment, id: Option[SummaryId] = None): UKPropertiesPrivateUseAdjustmentSummary = {
    UKPropertiesPrivateUseAdjustmentSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = privateUseAdjustment.amount
    )
  }
}

case class UKPropertiesTaxPaidSummary(summaryId: SummaryId, amount: BigDecimal) extends Summary with AmountHolder {

  val arrayName = UKPropertiesTaxPaidSummary.arrayName

  def toTaxPaid = TaxPaid(id = Some(summaryId), amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object UKPropertiesTaxPaidSummary {

  val arrayName = "taxesPaid"

  implicit val format = Json.format[UKPropertiesTaxPaidSummary]

  def toMongoSummary(taxPaid: TaxPaid, id: Option[SummaryId] = None): UKPropertiesTaxPaidSummary = {
    UKPropertiesTaxPaidSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = taxPaid.amount
    )
  }
}

case class UKProperties(id: BSONObjectID,
                        sourceId: SourceId,
                        nino: Nino,
                        taxYear: TaxYear,
                        lastModifiedDateTime: DateTime = DateTime.now(DateTimeZone.UTC),
                        createdDateTime: DateTime = DateTime.now(DateTimeZone.UTC),
                        rentARoomRelief: Option[BigDecimal] = None,
                        allowances: Option[Allowances] = None,
                        adjustments: Option[Adjustments] = None,
                        incomes: Seq[UKPropertiesIncomeSummary] = Nil,
                        expenses: Seq[UKPropertiesExpenseSummary] = Nil,
                        balancingCharges: Seq[UKPropertiesBalancingChargeSummary] = Nil,
                        privateUseAdjustment: Seq[UKPropertiesPrivateUseAdjustmentSummary] = Nil,
                        taxesPaid: Seq[UKPropertiesTaxPaidSummary] = Nil) extends SourceMetadata {
  def rentARoomReliefAmount = ValueOrZero(rentARoomRelief)

  def allowancesTotal = ValueOrZero(allowances.map(_.total))

  def lossBroughtForward = ValueOrZero(adjustments.flatMap(_.lossBroughtForward))

  def adjustedProfit = {
    PositiveOrZero(Total(incomes) + Total(balancingCharges) + Total(privateUseAdjustment) -
      Total(expenses) - allowancesTotal - rentARoomReliefAmount)
  }

  def taxPaid = Total(taxesPaid)

  def taxPaidPerProperty = taxesPaid.map(_.toTaxPaid)

  def toUKProperties = UKProperty(
    id = Some(sourceId),
    rentARoomRelief = rentARoomRelief,
    allowances = allowances,
    adjustments = adjustments)
}

object UKProperties {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[UKProperties], Json.writes[UKProperties])
  })

  def create(nino: Nino, taxYear: TaxYear, ukp: UKProperty): UKProperties = {
    val id = BSONObjectID.generate
    UKProperties(
      id = id,
      sourceId = id.stringify,
      nino = nino,
      taxYear = taxYear,
      rentARoomRelief = ukp.rentARoomRelief,
      allowances = ukp.allowances,
      adjustments = ukp.adjustments)
  }
}
