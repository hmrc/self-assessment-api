/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models

import play.api.libs.json.Format
import uk.gov.hmrc.selfassessmentapi.models.AccountingType._

object CessationReason extends Enumeration {
  type CessationReason = Value

  val BANKRUPTCY, DEATH, VOLUNTARY_CLOSURE, OTHERS = Value

  val toDes: CessationReason => String = {
    case CessationReason.BANKRUPTCY => "Bankruptcy"
    case CessationReason.DEATH => "Death"
    case CessationReason.VOLUNTARY_CLOSURE => "Voluntary Closure"
    case CessationReason.OTHERS => "Others"
  }

  val fromDes: String => Option[CessationReason] = {
    case "Bankruptcy" => Some(CessationReason.BANKRUPTCY)
    case "Death" => Some(CessationReason.DEATH)
    case "Voluntary Closure" => Some(CessationReason.VOLUNTARY_CLOSURE)
    case "Others" => Some(CessationReason.OTHERS)
    case _ => None
  }

  implicit val format: Format[CessationReason] =
    EnumJson.enumFormat(CessationReason, Some("CessationReason should be either BANKRUPTCY, DEATH, VOLUNTARY_CLOSURE or OTHERS"))
}