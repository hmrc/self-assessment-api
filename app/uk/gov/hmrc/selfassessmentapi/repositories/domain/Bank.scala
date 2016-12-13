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
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxYear, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.Interest
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.InterestType.InterestType

case class BankInterestSummary(summaryId: SummaryId, `type`: InterestType, amount: BigDecimal) extends Summary {
  override val arrayName: String = BankInterestSummary.arrayName

  def toBankInterest = Interest(Some(summaryId), `type`, amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> BSONString(summaryId),
    "type" -> BSONString(`type`.toString),
    "amount" -> BSONDouble(amount.doubleValue()))
}

object BankInterestSummary {

  val arrayName = "interests"

  implicit val format = Json.format[BankInterestSummary]

  def toMongoSummary(interest: Interest, id: Option[SummaryId] = None): BankInterestSummary = {
    BankInterestSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = interest.`type`,
      amount = interest.amount
    )
  }
}

case class Bank(id: BSONObjectID,
                sourceId: SourceId,
                nino: Nino,
                taxYear: TaxYear,
                lastModifiedDateTime: DateTime,
                createdDateTime: DateTime,
                interests: Seq[BankInterestSummary] = Seq.empty) extends SourceMetadata {

  def toBank = bank.Bank(Some(sourceId))
}

object Bank {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[Bank], Json.writes[Bank])
  })

  def create(nino: Nino, taxYear: TaxYear) = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)

    Bank(id, id.stringify, nino, taxYear, now, now)
  }
}
