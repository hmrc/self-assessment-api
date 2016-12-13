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

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYear

case class SelfAssessment(id: BSONObjectID,
                          nino: Nino,
                          taxYear: TaxYear,
                          createdDateTime: DateTime,
                          lastModifiedDateTime: DateTime,
                          taxYearProperties: Option[api.TaxYearProperties] = None)
  extends SelfAssessmentMetadata

object SelfAssessment {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity(
    Format(Json.reads[SelfAssessment], Json.writes[SelfAssessment]))
}
