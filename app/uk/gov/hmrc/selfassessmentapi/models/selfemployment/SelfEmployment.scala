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
import uk.gov.hmrc.selfassessmentapi.models._

case class SelfEmployment(id: Option[SourceId] = None,
                          accountingPeriod: AccountingPeriod,
                          accountingType: AccountingType,
                          commencementDate: LocalDate,
                          cessationDate: Option[LocalDate],
                          tradingName: String,
                          description: String,
                          address: Address)

object SelfEmployment {
  def from(desSelfEmployment: des.selfemployment.SelfEmployment): Option[SelfEmployment] = {
    for {
      accountingType <- AccountingType.fromDes(desSelfEmployment.cashOrAccruals)
      commencementDate <- desSelfEmployment.tradingStartDate
      address <- desSelfEmployment.addressDetails
    } yield
      SelfEmployment(
        id = desSelfEmployment.incomeSourceId,
        accountingPeriod = AccountingPeriod(start = LocalDate.parse(desSelfEmployment.accountingPeriodStartDate),
          end = LocalDate.parse(desSelfEmployment.accountingPeriodEndDate)),
        accountingType = accountingType,
        commencementDate = LocalDate.parse(commencementDate),
        cessationDate = None,
        tradingName = desSelfEmployment.tradingName,
        description = desSelfEmployment.typeOfBusiness.getOrElse(""), // FIXME: Not returned in DES response, it should be there...
        address = Address(address.addressLine1,
          address.addressLine2,
          address.addressLine3,
          address.addressLine4,
          address.postalCode,
          address.countryCode)
      )
  }

  val commencementDateValidator: Reads[LocalDate] = Reads
    .of[LocalDate]
    .filter(
      ValidationError("commencement date should be today or in the past", ErrorCode.DATE_NOT_IN_THE_PAST)
    )(date => date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now()))

  private def lengthIsBetween(minLength: Int, maxLength: Int): Reads[String] =
    Reads
      .of[String]
      .filter(
        ValidationError(s"field length must be between $minLength and $maxLength characters",
          ErrorCode.INVALID_FIELD_LENGTH))(name => name.length <= maxLength && name.length >= minLength)

  private val validateSIC: Reads[String] =
    Reads
      .of[String]
      .filter(ValidationError("business description must be a string that conforms to the UK SIC 2007 classifications",
        ErrorCode.INVALID_BUSINESS_DESCRIPTION))(name => sicClassifications.get.contains(name))


  implicit val writes: Writes[SelfEmployment] = Json.writes[SelfEmployment]

  implicit val reads: Reads[SelfEmployment] = (
    Reads.pure(None) and
      (__ \ "accountingPeriod").read[AccountingPeriod] and
      (__ \ "accountingType").read[AccountingType] and
      (__ \ "commencementDate").read[LocalDate](commencementDateValidator) and
      Reads.pure[Option[LocalDate]](None) and
      (__ \ "tradingName").read[String](lengthIsBetween(1, 105)) and
      (__ \ "description").read[String](validateSIC) and
      (__ \ "address").read[Address]
    ) (SelfEmployment.apply _)
}
