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

package uk.gov.hmrc.r2.selfassessmentapi.domain

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.r2.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.r2.selfassessmentapi.models

case class Dividends(id: BSONObjectID,
                     nino: Nino,
                     dividends: Map[TaxYear, models.dividends.Dividends],
                     lastModifiedDateTime: DateTime = DateTime.now(DateTimeZone.UTC)) extends LastModifiedDateTime {

  def dividendForTaxYear(taxYear: TaxYear): Option[models.dividends.Dividends] =
    dividends.get(taxYear)
}

object Dividends {
  import JsonFormatters.DividendsFormatters.annualSummaryMapFormat

  implicit val mongoFormats: Format[Dividends] = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[Dividends], Json.writes[Dividends])
  })
}
