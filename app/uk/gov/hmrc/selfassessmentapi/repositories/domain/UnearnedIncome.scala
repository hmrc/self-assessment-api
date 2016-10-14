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
import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome.Benefit
import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome.BenefitType.BenefitType
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxYear, _}

case class UnearnedIncomesBenefitSummary(summaryId: SummaryId,
                                         `type`: BenefitType,
                                         amount: BigDecimal,
                                         taxDeduction: BigDecimal) extends Summary {

  val arrayName = UnearnedIncomesBenefitSummary.arrayName

  def toBenefit: Benefit =
    Benefit(id = Some(summaryId),
      `type` = `type`,
      amount = amount,
      taxDeduction = taxDeduction)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "taxDeduction" -> BSONDouble(taxDeduction.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object UnearnedIncomesBenefitSummary {

  val arrayName = "benefits"

  implicit val format = Json.format[UnearnedIncomesBenefitSummary]

  def toMongoSummary(income: Benefit, id: Option[SummaryId] = None): UnearnedIncomesBenefitSummary = {
    UnearnedIncomesBenefitSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = income.`type`,
      amount = income.amount,
      taxDeduction = income.taxDeduction
    )
  }
}

case class UnearnedIncome(id: BSONObjectID,
                          sourceId: SourceId,
                          saUtr: SaUtr,
                          taxYear: TaxYear,
                          lastModifiedDateTime: DateTime,
                          createdDateTime: DateTime,
                          benefits: Seq[UnearnedIncomesBenefitSummary] = Nil) extends SourceMetadata {

  def toUnearnedIncome = unearnedincome.UnearnedIncome(id = Some(sourceId))
}

object UnearnedIncome {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[UnearnedIncome], Json.writes[UnearnedIncome])
  })

  def create(saUtr: SaUtr, taxYear: TaxYear, se: unearnedincome.UnearnedIncome): UnearnedIncome = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)
    UnearnedIncome(
      id = id,
      sourceId = id.stringify,
      saUtr = saUtr,
      taxYear = taxYear,
      lastModifiedDateTime = now,
      createdDateTime = now
      )
  }
}
