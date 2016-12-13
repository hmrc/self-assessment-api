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

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.{BSONDocument, BSONDouble, BSONObjectID, BSONString}
import uk.gov.hmrc.domain._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContribution
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.BalancingChargeType.BalancingChargeType
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment._
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.{Adjustments, Allowances}

case class SelfEmploymentIncomeSummary(summaryId: SummaryId,
                                       `type`: IncomeType,
                                       amount: BigDecimal) extends Summary with AmountHolder {
  val arrayName = SelfEmploymentIncomeSummary.arrayName

  def toIncome: Income =
    Income(id = Some(summaryId),
      `type` = `type`,
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object SelfEmploymentIncomeSummary {

  val arrayName = "incomes"

  implicit val format = Json.format[SelfEmploymentIncomeSummary]

  def toMongoSummary(income: Income, id: Option[SummaryId] = None): SelfEmploymentIncomeSummary = {
    SelfEmploymentIncomeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = income.`type`,
      amount = income.amount
    )
  }
}

case class SelfEmploymentExpenseSummary(summaryId: SummaryId,
                                        `type`: ExpenseType,
                                        amount: BigDecimal,
                                        disallowableAmount: BigDecimal) extends Summary with AmountHolder {
  val arrayName = SelfEmploymentExpenseSummary.arrayName

  def toExpense: Expense =
    Expense(id = Some(summaryId),
      `type` = `type`,
      amount = amount,
      disallowableAmount = disallowableAmount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "disallowableAmount" -> BSONDouble(disallowableAmount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object SelfEmploymentExpenseSummary {

  val arrayName = "expenses"

  implicit val format = Json.format[SelfEmploymentExpenseSummary]

  def toMongoSummary(expense: Expense, id: Option[SummaryId] = None): SelfEmploymentExpenseSummary = {
    SelfEmploymentExpenseSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = expense.`type`,
      amount = expense.amount,
      disallowableAmount = expense.disallowableAmount
    )
  }
}

case class SelfEmploymentBalancingChargeSummary(summaryId: SummaryId,
                                                `type`: BalancingChargeType,
                                                amount: BigDecimal) extends Summary with AmountHolder {
  val arrayName = SelfEmploymentBalancingChargeSummary.arrayName

  def toBalancingCharge =
    BalancingCharge(id = Some(summaryId),
      `type` = `type`,
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object SelfEmploymentBalancingChargeSummary {

  val arrayName = "balancingCharges"

  implicit val format = Json.format[SelfEmploymentBalancingChargeSummary]

  def toMongoSummary(balancingCharge: BalancingCharge, id: Option[SummaryId] = None): SelfEmploymentBalancingChargeSummary = {
    SelfEmploymentBalancingChargeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = balancingCharge.`type`,
      amount = balancingCharge.amount
    )
  }
}

case class SelfEmploymentGoodsAndServicesOwnUseSummary(summaryId: SummaryId, amount: BigDecimal) extends Summary with AmountHolder {

  val arrayName = SelfEmploymentGoodsAndServicesOwnUseSummary.arrayName

  def toGoodsAndServicesOwnUse = GoodsAndServicesOwnUse(id = Some(summaryId), amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue())
  )
}

object SelfEmploymentGoodsAndServicesOwnUseSummary {

  val arrayName = "goodsAndServicesOwnUse"

  implicit val format = Json.format[SelfEmploymentGoodsAndServicesOwnUseSummary]

  def toMongoSummary(goodsAndServicesOwnUse: GoodsAndServicesOwnUse, id: Option[SummaryId] = None): SelfEmploymentGoodsAndServicesOwnUseSummary = {
    SelfEmploymentGoodsAndServicesOwnUseSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      amount = goodsAndServicesOwnUse.amount
    )
  }
}

case class TaxYearProperties(id: BSONObjectID,
                             nino: Nino,
                             taxYear: TaxYear,
                             lastModifiedDateTime: DateTime,
                             createdDateTime: DateTime,
                             pensionContributions: Option[PensionContribution] = None)
  extends SelfAssessmentMetadata {

  def toTaxYearProperties = api.TaxYearProperties(
    id = Some(id.stringify),
    pensionContributions = pensionContributions)
}

object TaxYearProperties {

  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[TaxYearProperties], Json.writes[TaxYearProperties])
  })
}

case class SelfEmployment(id: BSONObjectID,
                          sourceId: SourceId,
                          nino: Nino,
                          taxYear: TaxYear,
                          lastModifiedDateTime: DateTime,
                          createdDateTime: DateTime,
                          commencementDate: LocalDate,
                          allowances: Option[Allowances] = None,
                          adjustments: Option[Adjustments] = None,
                          incomes: Seq[SelfEmploymentIncomeSummary] = Nil,
                          expenses: Seq[SelfEmploymentExpenseSummary] = Nil,
                          balancingCharges: Seq[SelfEmploymentBalancingChargeSummary] = Nil,
                          goodsAndServicesOwnUse: Seq[SelfEmploymentGoodsAndServicesOwnUseSummary] = Nil) extends SourceMetadata {

  def adjustedProfits: BigDecimal = PositiveOrZero(profitIncreases - profitReductions)

  private def profitIncreases: BigDecimal = {
    val adjustments = this.adjustments.map { a =>
      Sum(a.basisAdjustment, a.accountingAdjustment, a.averagingAdjustment)
    }.getOrElse(BigDecimal(0))

    Total(incomes) + Total(balancingCharges) + Total(goodsAndServicesOwnUse) + adjustments
  }

  private def profitReductions: BigDecimal = {
    val expenses = Some(this.expenses.filterNot(_.`type` == selfemployment.ExpenseType.Depreciation).map(_.amount).sum)
    val allowances = this.allowances.map(_.total)
    val adjustments = this.adjustments.map { a => Sum(a.includedNonTaxableProfits, a.overlapReliefUsed) }

    Sum(expenses, allowances, adjustments)
  }

  def lossBroughtForward = adjustments.flatMap(_.lossBroughtForward).getOrElse(BigDecimal(0))

  def toSelfEmployment = selfemployment.SelfEmployment(
    id = Some(sourceId),
    commencementDate = commencementDate,
    allowances = allowances,
    adjustments = adjustments)

  lazy val outstandingBusinessIncome = ValueOrZero(adjustments.flatMap(_.outstandingBusinessIncome))
}

object SelfEmployment {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[SelfEmployment], Json.writes[SelfEmployment])
  })

  def create(nino: Nino, taxYear: TaxYear = TaxYear(""), se: selfemployment.SelfEmployment): SelfEmployment = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)
    SelfEmployment(
      id = id,
      sourceId = id.stringify,
      nino = nino,
      taxYear = taxYear,
      lastModifiedDateTime = now,
      createdDateTime = now,
      commencementDate = se.commencementDate,
      allowances = se.allowances,
      adjustments = se.adjustments)
  }
}
