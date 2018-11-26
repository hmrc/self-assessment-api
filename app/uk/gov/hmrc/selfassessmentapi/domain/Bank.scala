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

package uk.gov.hmrc.selfassessmentapi.domain

import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.models.banks.BankAnnualSummary
import uk.gov.hmrc.selfassessmentapi.models.{TaxYear, banks}

case class Bank(id: BSONObjectID,
                sourceId: String,
                nino: Nino,
                lastModifiedDateTime: DateTime,
                accountName: Option[String],
                annualSummaries: Map[TaxYear, BankAnnualSummary])
    extends LastModifiedDateTime {

  def toModel(elideID: Boolean = false): banks.Bank = {
    val id = if (elideID) None else Some(sourceId)
    banks.Bank(id, accountName)
  }

  def annualSummary(taxYear: TaxYear): BankAnnualSummary =
    annualSummaries.getOrElse(taxYear, BankAnnualSummary(None, None))
}

object Bank {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.BankFormatters.annualSummaryMapFormat

  implicit val mongoFormats: Format[Bank] = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[Bank], Json.writes[Bank])
  })
}
