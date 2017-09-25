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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.Class4NicInfo


case class NonFinancials(class4NicInfo: Option[Class4NicInfo], payVoluntaryClass2Nic: Option[Boolean])

object NonFinancials {
  implicit val writes = Json.writes[NonFinancials]

  implicit val reads: Reads[NonFinancials] = (
    (__ \ "class4NicInfo").readNullable[Class4NicInfo] and
      (__ \ "payVoluntaryClass2Nic").readNullable[Boolean]
    ) (NonFinancials.apply _)
}
