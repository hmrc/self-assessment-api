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

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode._

case class Class4NicInfo(isExempt: Option[Boolean], exemptionCode: Option[String])


object Class4NicInfo {

  implicit val writes = Json.writes[Class4NicInfo]

  val exemptionCodes = Seq("001", "002", "003", "004", "005", "006")

  implicit val reads: Reads[Class4NicInfo] = (
      (__ \ "isExempt").readNullable[Boolean] and
      (__ \ "exemptionCode").readNullable[String]
    )(Class4NicInfo.apply _)
    .filter(ValidationError(s"Exemption code must be present", MANDATORY_FIELD_MISSING))(isExemptionCodePresent)
    .filter(ValidationError(s"Invalid exemption code. Valid codes are: ${exemptionCodes.mkString(",")}",
    INVALID_VALUE))(isExemptionCodeValid)

  private def isExemptionCodePresent(class4Exemption: Class4NicInfo) =
    (class4Exemption.isExempt, class4Exemption.exemptionCode) match {
      case (Some(true), None) => false
      case _ => true
    }

  private def isExemptionCodeValid(class4Exemption: Class4NicInfo) = {
    class4Exemption.isExempt.fold(true) {
      isExempt =>
        if (isExempt)
          exemptionCodes.contains(class4Exemption.exemptionCode.getOrElse(""))
        else true
    }
  }

}
