/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Income(amount: BigDecimal, taxDeducted: Option[BigDecimal])

object Income {
  implicit val reads: Reads[Income] = (
    (__ \ "amount").read[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "taxDeducted").readNullable[BigDecimal](nonNegativeAmountValidator)
    ) (Income.apply _)


  implicit val writes: Writes[Income] = Json.writes[Income]
}
