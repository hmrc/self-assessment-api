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

package uk.gov.hmrc.selfassessmentapi.models.des.selfemployment

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.CessationReason.CessationReason
import uk.gov.hmrc.selfassessmentapi.models.{AccountingType, des}
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.Address

case class Business(businessDetails: Seq[SelfEmployment])

object Business {
  implicit val writes: Writes[Business] = Json.writes[Business]

  def from(apiSelfEmployment: models.selfemployment.SelfEmployment): Business = {
    Business(
      Seq(
        SelfEmployment(incomeSourceId = None,
                       accountingPeriodStartDate = apiSelfEmployment.accountingPeriod.start.toString,
                       accountingPeriodEndDate = apiSelfEmployment.accountingPeriod.end.toString,
                       tradingName = apiSelfEmployment.tradingName,
                       addressDetails = Some(SelfEmploymentAddress.from(apiSelfEmployment.address)),
                       typeOfBusiness = Some(apiSelfEmployment.description),
                       tradingStartDate = Some(apiSelfEmployment.commencementDate.toString),
                       cashOrAccruals = AccountingType.toDes(apiSelfEmployment.accountingType))))
  }

}

case class SelfEmployment(incomeSourceId: Option[String],
                          accountingPeriodStartDate: String,
                          accountingPeriodEndDate: String,
                          tradingName: String,
                          addressDetails: Option[SelfEmploymentAddress],
                          typeOfBusiness: Option[String],
                          tradingStartDate: Option[String],
                          cashOrAccruals: String,
                          cessationReason: Option[String] = None,
                          cessationDate: Option[String] = None)

object SelfEmployment {
  implicit val writes: Writes[SelfEmployment] = Json.writes[SelfEmployment]

  implicit val reads: Reads[SelfEmployment] = (
    (__ \ "incomeSourceId").readNullable[String] and
      (__ \ "accountingPeriodStartDate").read[String] and
      (__ \ "accountingPeriodEndDate").read[String] and
      (__ \ "tradingName").read[String] and
      (__ \ "businessAddressDetails").readNullable[SelfEmploymentAddress] and
      (__ \ "typeOfBusiness").readNullable[String] and
      (__ \ "tradingStartDate").readNullable[String] and
      (__ \ "cashOrAccruals").read[String] and
      (__ \ "cessationReason").readNullable[String] and
      (__ \ "cessationDate").readNullable[String]
  )(SelfEmployment.apply _)

  def from(apiSelfEmployment: models.selfemployment.SelfEmployment): SelfEmployment = {
    SelfEmployment(incomeSourceId = None,
                   accountingPeriodStartDate = apiSelfEmployment.accountingPeriod.start.toString,
                   accountingPeriodEndDate = apiSelfEmployment.accountingPeriod.end.toString,
                   tradingName = apiSelfEmployment.tradingName,
                   addressDetails = Some(SelfEmploymentAddress.from(apiSelfEmployment.address)),
                   typeOfBusiness = Some(apiSelfEmployment.description),
                   tradingStartDate = Some(apiSelfEmployment.commencementDate.toString),
                   cashOrAccruals = AccountingType.toDes(apiSelfEmployment.accountingType))
  }

}

case class SelfEmploymentAddress(addressLine1: String,
                                 addressLine2: Option[String],
                                 addressLine3: Option[String],
                                 addressLine4: Option[String],
                                 postalCode: Option[String],
                                 countryCode: String = "GB")

object SelfEmploymentAddress {
  implicit val writes: Writes[SelfEmploymentAddress] = Json.writes[SelfEmploymentAddress]
  implicit val reads: Reads[SelfEmploymentAddress] = Json.reads[SelfEmploymentAddress]

  def from(address: Address): SelfEmploymentAddress = {
    SelfEmploymentAddress(address.lineOne,
      address.lineTwo,
      address.lineThree,
      address.lineFour,
      address.postalCode,
      address.countryCode)
  }
}
