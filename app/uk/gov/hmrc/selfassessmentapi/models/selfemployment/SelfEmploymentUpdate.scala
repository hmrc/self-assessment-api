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
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.selfassessmentapi.models.AccountingType._
import uk.gov.hmrc.selfassessmentapi.models.CessationReason.CessationReason
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, ErrorCode, sicClassifications, _}


case class SelfEmploymentUpdate(accountingPeriod: AccountingPeriod,
                                accountingType: AccountingType,
                                commencementDate: LocalDate,
                                cessationDate: Option[LocalDate],
                                cessationReason: Option[CessationReason],
                                tradingName: String,
                                description: String,
                                address: Address,
                                contactDetails: Option[ContactDetails],
                                paperless: Boolean,
                                seasonal: Boolean)

object SelfEmploymentUpdate {

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
      (__ \ "description").read[String](validateSIC) and //FIXME Need to revisit and fix the format after confirmation from business
      (__ \ "address").read[Address] and
      (__ \ "contactDetails").readNullable[ContactDetails] and
      (__ \ "paperless").read[Boolean] and
      (__ \ "seasonal").read[Boolean]
    ) (SelfEmploymentUpdate.apply _)
}

case class ContactDetails(primaryPhoneNumber: Option[String],
                          secondaryPhoneNumber: Option[String],
                          faxNumber: Option[String],
                          emailAddress: Option[String])

object ContactDetails {
  def contactDetailsRegex(maxLength: Int) = s"^[A-Z0-9 )/(*#-]{1,$maxLength}$$"

  private val emailValidator: Reads[String] =
    Reads.of[String].filter(ValidationError("Email should be valid and must be 3 to 132 characters", ErrorCode.INVALID_FIELD_FORMAT)
    )(email => email.length >= 3 && email.length <= 132 && EmailAddress.isValid(email)
  )


  implicit val writes: Writes[ContactDetails] = Json.writes[ContactDetails]

  implicit val reads: Reads[ContactDetails] = (
    (__ \ "primaryPhoneNumber").readNullable[String](regexValidator("primaryPhoneNumber", contactDetailsRegex(24))) and
      (__ \ "secondaryPhoneNumber").readNullable[String](regexValidator("secondaryPhoneNumber", contactDetailsRegex(24))) and
      (__ \ "faxNumber").readNullable[String](regexValidator("faxNumber", contactDetailsRegex(24))) and
      (__ \ "emailAddress").readNullable[String](emailValidator)
    ) (ContactDetails.apply _)

}