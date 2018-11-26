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

package uk.gov.hmrc.r2.selfassessmentapi.models.dividends

import play.api.libs.json._
import uk.gov.hmrc.r2.selfassessmentapi.models.nonNegativeAmountValidator

case class Dividends(ukDividends: Option[BigDecimal])

object Dividends {
  implicit val reads: Reads[Dividends] =
    (__ \ "ukDividends").readNullable[BigDecimal](nonNegativeAmountValidator).map(Dividends(_))

  implicit val writes: Writes[Dividends] = Json.writes[Dividends]
}
