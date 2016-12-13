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
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.Income
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.BenefitType.BenefitType
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxYear, _}

case class BenefitIncomeSummary(summaryId: SummaryId,
                                `type`: BenefitType,
                                amount: BigDecimal,
                                taxDeduction: BigDecimal) extends Summary {

  val arrayName = BenefitIncomeSummary.arrayName

  def toBenefit: Income =
    Income(id = Some(summaryId),
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

object BenefitIncomeSummary {

  val arrayName = "incomes"

  implicit val format = Json.format[BenefitIncomeSummary]

  def toMongoSummary(income: Income, id: Option[SummaryId] = None): BenefitIncomeSummary = {
    BenefitIncomeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = income.`type`,
      amount = income.amount,
      taxDeduction = income.taxDeduction
    )
  }
}

case class Benefits(id: BSONObjectID,
                    sourceId: SourceId,
                    nino: Nino,
                    taxYear: TaxYear,
                    lastModifiedDateTime: DateTime,
                    createdDateTime: DateTime,
                    incomes: Seq[BenefitIncomeSummary] = Nil) extends SourceMetadata {

  def toUnearnedIncome = benefit.Benefit(id = Some(sourceId))
}

object Benefits {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[Benefits], Json.writes[Benefits])
  })

  def create(nino: Nino, taxYear: TaxYear, se: benefit.Benefit): Benefits = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)
    Benefits(
      id = id,
      sourceId = id.stringify,
      nino = nino,
      taxYear = taxYear,
      lastModifiedDateTime = now,
      createdDateTime = now)
  }
}
