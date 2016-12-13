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

package uk.gov.hmrc.selfassessmentapi.resources.models

import com.github.nscala_time.time.OrderingImplicits
import org.joda.time.LocalDate
import play.api.libs.json.Json

case class PeriodSummary(id: PeriodId, from: LocalDate, to: LocalDate) extends Period

object PeriodSummary {
  private implicit val localDateOrdering = OrderingImplicits.LocalDateOrdering

  implicit val ordering: Ordering[PeriodSummary] = Ordering.by(_.from)
  implicit val writes = Json.writes[PeriodSummary]
}
