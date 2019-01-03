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

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.r2.selfassessmentapi.models.Class4NicsExemptionCode.Class4NicsExemptionCode
import uk.gov.hmrc.r2.selfassessmentapi.models.ErrorCode._

case class Class4NicInfo(isExempt: Option[Boolean], exemptionCode: Option[Class4NicsExemptionCode])


object Class4NicsExemptionCode extends Enumeration {
  type Class4NicsExemptionCode = Value
  val NON_RESIDENT = Value("001")
  val TRUSTEE = Value("002")
  val DIVER = Value("003")
  val ITTOIA_2005 = Value("004")
  val OVER_STATE_PENSION_AGE = Value("005")
  val UNDER_16 = Value("006")
  implicit val format = EnumJson.enumFormat(Class4NicsExemptionCode,
    Some(s"Class 4 NICs exemption code should be one of: ${Class4NicsExemptionCode.values.mkString(", ")}"))
}

object Class4NicInfo {

  implicit val writes = Json.writes[Class4NicInfo]

  implicit val reads: Reads[Class4NicInfo] = (
      (__ \ "isExempt").readNullable[Boolean] and
      (__ \ "exemptionCode").readNullable[Class4NicsExemptionCode]
    )(Class4NicInfo.apply _)
    .filter(ValidationError(s"Empty class4NicInfo element provided", INVALID_VALUE))(nonEmptyClass4NicInfo)
    .filter(ValidationError(s"Exemption code must be present only if the exempt flag is set to true", INVALID_VALUE))(exemptionCodeOnlyPresentFails)
    .filter(ValidationError(s"Exemption code value must be present if the exempt flag is set to true", MANDATORY_FIELD_MISSING))(isExemptionCodePresent)
    .filter(ValidationError(s"Exemption code value must not be present if the exempt flag is set to false", INVALID_VALUE))(isExemptionCodeAbsent)

  private def nonEmptyClass4NicInfo(class4Exemption: Class4NicInfo) =
    (class4Exemption.isExempt, class4Exemption.exemptionCode) match {
      case (None, None) => false
      case _ => true
    }

  private def exemptionCodeOnlyPresentFails(class4Exemption: Class4NicInfo) =
    (class4Exemption.isExempt, class4Exemption.exemptionCode) match {
      case (None, Some(_)) => false
      case _ => true
    }

  private def isExemptionCodePresent(class4Exemption: Class4NicInfo) =
    (class4Exemption.isExempt, class4Exemption.exemptionCode) match {
      case (Some(true), None) => false
      case _ => true
    }

  private def isExemptionCodeAbsent(class4Exemption: Class4NicInfo) =
    (class4Exemption.isExempt, class4Exemption.exemptionCode) match {
      case (Some(false), Some(_)) => false
      case _ => true
    }
}
