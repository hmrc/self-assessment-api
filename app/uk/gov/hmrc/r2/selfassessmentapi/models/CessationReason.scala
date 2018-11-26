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

package uk.gov.hmrc.r2.selfassessmentapi.models

import play.api.libs.json.Format
import uk.gov.hmrc.r2.selfassessmentapi.models

object CessationReason extends Enumeration {
  type CessationReason = Value

  val Retirement: models.CessationReason.Value = Value("001")
  val Emigration: models.CessationReason.Value = Value("002")
  val BusinessIncorporated: models.CessationReason.Value = Value("003")
  val BusinessHasBecomeAPartnership: models.CessationReason.Value = Value("004")
  val Bankruptcy: models.CessationReason.Value = Value("005")
  val Other: models.CessationReason.Value = Value("006")
  val DontWantToSay: models.CessationReason.Value = Value("007")
  val Deceased: models.CessationReason.Value = Value("008")

  implicit val format: Format[CessationReason] =
    EnumJson.enumFormat(CessationReason, Some(s"CessationReason should one of: ${CessationReason.values.mkString(", ")}"))
}
