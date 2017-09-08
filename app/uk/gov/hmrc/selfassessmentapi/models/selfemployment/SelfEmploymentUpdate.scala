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

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.AccountingType._
import uk.gov.hmrc.selfassessmentapi.models.CessationReason.CessationReason
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, ErrorCode, sicClassifications, _}


case class SelfEmploymentUpdate(accountingPeriod: AccountingPeriod,
                                accountingType: AccountingType,
                                commencementDate: LocalDate,
                                cessationDate: Option[LocalDate],
                                cessationReason: Option[CessationReason],
                                tradingName: String,
                                businessDescription: String,
                                businessAddressLineOne: String,
                                businessAddressLineTwo: Option[String],
                                businessAddressLineThree: Option[String],
                                businessAddressLineFour: Option[String],
                                businessPostcode: Option[String],
                                businessCountry: String,
                                contactDetails: Option[ContactDetails],
                                paperless: Boolean,
                                seasonal: Boolean)

object SelfEmploymentUpdate {

  def stringRegex(maxLength: Int) = s"^[A-Za-z0-9 \\-,.&'\\/]{1,$maxLength}$$"

  def lengthIs(length: Int): Reads[String] =
    Reads.of[String].filter(ValidationError(s"field length must be $length characters", ErrorCode.INVALID_FIELD_LENGTH)
    )(name => name.length == length)

  private val validateSIC: Reads[String] =
    Reads.of[String].filter(ValidationError("business description must be a string that conforms to the UK SIC 2007 classifications", ErrorCode.INVALID_BUSINESS_DESCRIPTION)
    )(name => sicClassifications.get.contains(name))


  implicit val writes: Writes[SelfEmploymentUpdate] = Json.writes[SelfEmploymentUpdate]

  implicit val reads: Reads[SelfEmploymentUpdate] = (
    (__ \ "accountingPeriod").read[AccountingPeriod] and
      (__ \ "accountingType").read[AccountingType] and
      (__ \ "commencementDate").read[LocalDate] and
      (__ \ "cessationDate").readNullable[LocalDate] and
      (__ \ "cessationReason").readNullable[CessationReason] and
      (__ \ "tradingName").read[String](regexValidator("tradingName", stringRegex(105))) and
      (__ \ "businessDescription").read[String](validateSIC) and //FIXME Need to revisit and fix the format after confirmation from business
      (__ \ "businessAddressLineOne").read[String](regexValidator("businessAddressLineOne", stringRegex(35))) and
      (__ \ "businessAddressLineTwo").readNullable[String](regexValidator("businessAddressLineTwo", stringRegex(35))) and
      (__ \ "businessAddressLineThree").readNullable[String](regexValidator("businessAddressLineThree", stringRegex(35))) and
      (__ \ "businessAddressLineFour").readNullable[String](regexValidator("businessAddressLineFour", stringRegex(35))) and
      (__ \ "businessPostcode").readNullable[String](postcodeValidator) and
      (__ \ "businessCountry").read[String](lengthIs(2)) and
      (__ \ "contactDetails").readNullable[ContactDetails] and
      (__ \ "paperless").read[Boolean] and
      (__ \ "seasonal").read[Boolean]
    ) (SelfEmploymentUpdate.apply _).filter(
    ValidationError(
      "businessPostcode mandatory when businessCountry = GB",
      ErrorCode.MANDATORY_FIELD_MISSING)) { se =>
    if (se.businessCountry == "GB") se.businessPostcode.isDefined
    else true
  }
}

case class ContactDetails(contactPrimaryPhoneNumber: Option[String],
                          contactSecondaryPhoneNumber: Option[String],
                          contactFaxNumber: Option[String],
                          contactEmailAddress: Option[String])

object ContactDetails {
  def contactDetailsRegex(maxLength: Int) = s"^[A-Z0-9 )/(*#-]+{1,$maxLength}$$"

  def lengthIsBetween(minLength: Int, maxLength: Int): Reads[String] =
    Reads.of[String].filter(ValidationError(s"field length must be between $minLength and $maxLength characters", ErrorCode.INVALID_FIELD_LENGTH)
    )(name => name.length <= maxLength && name.length >= minLength)

  implicit val writes: Writes[ContactDetails] = Json.writes[ContactDetails]

  implicit val reads: Reads[ContactDetails] = (
    (__ \ "contactPrimaryPhoneNumber").readNullable[String](regexValidator("contactPrimaryPhoneNumber", contactDetailsRegex(24))) and
      (__ \ "contactSecondaryPhoneNumber").readNullable[String](regexValidator("contactSecondaryPhoneNumber", contactDetailsRegex(24))) and
      (__ \ "contactFaxNumber").readNullable[String](regexValidator("contactFaxNumber", contactDetailsRegex(24))) and
      (__ \ "contactEmailAddress").readNullable[String](lengthIsBetween(3, 132))
    ) (ContactDetails.apply _)

}