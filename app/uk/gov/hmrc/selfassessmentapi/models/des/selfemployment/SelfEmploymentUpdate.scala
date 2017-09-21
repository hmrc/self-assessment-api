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

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.AccountingType
import uk.gov.hmrc.selfassessmentapi.models.selfemployment

case class SelfEmploymentUpdate(nino: Option[String] = None,
                                utr: Option[String] = None,
                                accountingPeriodStartDate: String,
                                accountingPeriodEndDate: String,
                                tradingName: String,
                                addressDetails: SelfEmploymentAddress,
                                contactDetails: Option[ContactDetails] = None,
                                typeOfBusiness: String,
                                tradingStartDate: String,
                                cashOrAccruals: String,
                                paperless: Boolean,
                                seasonal: Boolean,
                                cessationDate: Option[String] = None,
                                effectiveDate: String,
                                reasonForCessation: Option[String] = None,
                                agentId: Option[String] = None,
                                changedDate: Option[String] = None,
                                incomeSource: Option[String] = None)

object SelfEmploymentUpdate {
  implicit val writes: Writes[SelfEmploymentUpdate] = Json.writes[SelfEmploymentUpdate]

  def from(apiSelfEmployment: models.selfemployment.SelfEmploymentUpdate): SelfEmploymentUpdate = {
    SelfEmploymentUpdate(
      accountingPeriodStartDate = apiSelfEmployment.accountingPeriod.start.toString,
      accountingPeriodEndDate = apiSelfEmployment.accountingPeriod.end.toString,
      tradingName = apiSelfEmployment.tradingName,
      addressDetails = SelfEmploymentAddress(
        apiSelfEmployment.address.lineOne,
        apiSelfEmployment.address.lineTwo,
        apiSelfEmployment.address.lineThree,
        apiSelfEmployment.address.lineFour,
        apiSelfEmployment.address.postalCode,
        apiSelfEmployment.address.countryCode),
      contactDetails = ContactDetails.from(apiSelfEmployment.contactDetails),
      typeOfBusiness = apiSelfEmployment.description,
      tradingStartDate = apiSelfEmployment.commencementDate.toString,
      cashOrAccruals = AccountingType.toDes(apiSelfEmployment.accountingType),
      paperless = apiSelfEmployment.paperless,
      seasonal = apiSelfEmployment.seasonal,
      cessationDate = apiSelfEmployment.cessationReason.map(_ => apiSelfEmployment.effectiveDate.toString),
      reasonForCessation = apiSelfEmployment.cessationReason.map(_.toString),
      effectiveDate = apiSelfEmployment.effectiveDate.toString
    )
  }

}

case class ContactDetails(primaryPhoneNumber: Option[String],
                          secondaryPhoneNumber: Option[String],
                          faxNumber: Option[String],
                          emailAddress: Option[String])

object ContactDetails {
  implicit val writes: Writes[ContactDetails] = Json.writes[ContactDetails]

  def from(contactDetails: Option[selfemployment.ContactDetails]): Option[ContactDetails] =
    contactDetails map { cd =>
      ContactDetails(cd.primaryPhoneNumber, cd.secondaryPhoneNumber, cd.faxNumber, cd.emailAddress)
    }
}
