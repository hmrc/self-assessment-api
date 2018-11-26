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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import org.joda.time.LocalDate
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.selfassessmentapi.models.SourceId

case class Eops(from: LocalDate, to: LocalDate, due: LocalDate, status: String)

object Eops {
  implicit val writes: Writes[Eops] = Json.writes[Eops]
}

case class SelfEmploymentStatementRetrieve(id: Option[SourceId], eops: Seq[Eops])

object SelfEmploymentStatementRetrieve {
  implicit val writes: Writes[SelfEmploymentStatementRetrieve] = Json.writes[SelfEmploymentStatementRetrieve]
}

case class SelfEmploymentStatementsRetrieve(statements: Seq[SelfEmploymentStatementRetrieve])

object SelfEmploymentStatementsRetrieve {
  implicit val writes: Writes[SelfEmploymentStatementsRetrieve] = Json.writes[SelfEmploymentStatementsRetrieve]
}
