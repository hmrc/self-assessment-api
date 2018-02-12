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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import org.joda.time.LocalDate
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.models.AccountingType._
import uk.gov.hmrc.selfassessmentapi.models._

case class SelfEmployment(id: Option[SourceId] = None,
                          accountingPeriod: AccountingPeriod,
                          accountingType: AccountingType,
                          commencementDate: LocalDate,
                          cessationDate: Option[LocalDate],
                          tradingName: String,
                          businessDescription: String,
                          businessAddressLineOne: String,
                          businessAddressLineTwo: Option[String],
                          businessAddressLineThree: Option[String],
                          businessAddressLineFour: Option[String],
                          businessPostcode: String)

object SelfEmployment {

  lazy val validateSICEnabled: Boolean = FeatureSwitch(AppContext.featureSwitch).sicValidationEnabled

  def from(desSelfEmployment: des.selfemployment.SelfEmployment): Option[SelfEmployment] = {
    for {
      accountingType <- AccountingType.fromDes(desSelfEmployment.cashOrAccruals)
      commencementDate <- desSelfEmployment.tradingStartDate
      address <- desSelfEmployment.addressDetails
      addressPostcode <- address.postalCode
    } yield
      SelfEmployment(
        id = desSelfEmployment.incomeSourceId,
        accountingPeriod = AccountingPeriod(start = LocalDate.parse(desSelfEmployment.accountingPeriodStartDate),
          end = LocalDate.parse(desSelfEmployment.accountingPeriodEndDate)),
        accountingType = accountingType,
        commencementDate = LocalDate.parse(commencementDate),
        cessationDate = None,
        tradingName = desSelfEmployment.tradingName,
        businessDescription = desSelfEmployment.typeOfBusiness.getOrElse(""), // FIXME: Not returned in DES response, it should be there...
        businessAddressLineOne = address.addressLine1,
        businessAddressLineTwo = address.addressLine2,
        businessAddressLineThree = address.addressLine3,
        businessAddressLineFour = address.addressLine4,
        businessPostcode = addressPostcode)
  }

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
      (__ \ "businessDescription").read[String]
         { if (validateSICEnabled) validateSIC else Reads.of[String] }
        and
      (__ \ "businessAddressLineOne").read[String](lengthIsBetween(1, 35)) and
      (__ \ "businessAddressLineTwo").readNullable[String](lengthIsBetween(1, 35)) and
      (__ \ "businessAddressLineThree").readNullable[String](lengthIsBetween(1, 35)) and
      (__ \ "businessAddressLineFour").readNullable[String](lengthIsBetween(1, 35)) and
      (__ \ "businessPostcode").read[String](lengthIsBetween(1, 10) keepAnd postcodeValidator)
    )(SelfEmployment.apply _)
}
