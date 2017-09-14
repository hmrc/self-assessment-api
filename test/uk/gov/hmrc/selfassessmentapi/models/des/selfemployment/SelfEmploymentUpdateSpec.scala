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

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, AccountingType}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.Address

class SelfEmploymentUpdateSpec extends JsonSpec {
  "from" should {

    val apiUpdate = models.selfemployment.SelfEmploymentUpdate(accountingPeriod = AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")),
                                                                accountingType = AccountingType.CASH,
                                                                commencementDate = LocalDate.parse("2017-04-01"),
                                                                cessationDate = None,
                                                                cessationReason = None,
                                                                tradingName = "Foo Consulting",
                                                                description = "Absorbable haemostatics (manufacture)",
                                                                address = Address("17 Profitable Road",
                                                                                  Some("Sussex"),
                                                                                  Some("UK"),
                                                                                  None,
                                                                                  Some("W11 7QT"),
                                                                                  "GB"),
                                                                contactDetails = Some(selfemployment.ContactDetails(primaryPhoneNumber = Some( "077723232"),
                                                                  secondaryPhoneNumber =  Some("077723232"),
                                                                  faxNumber =  Some("077723232"),
                                                                  emailAddress =  Some("admin@mail.com"))),
                                                                paperless = false,
                                                                seasonal = false)

    val desUpdate = SelfEmploymentUpdate.from(apiUpdate)

    "correctly map a API self-employment update into a DES self-employment update" in {
      desUpdate.accountingPeriodStartDate shouldBe apiUpdate.accountingPeriod.start.toString
      desUpdate.accountingPeriodEndDate shouldBe apiUpdate.accountingPeriod.end.toString
      desUpdate.cashOrAccruals shouldBe AccountingType.toDes(apiUpdate.accountingType)
      desUpdate.tradingStartDate shouldBe apiUpdate.commencementDate.toString
      desUpdate.tradingName shouldBe apiUpdate.tradingName
      desUpdate.typeOfBusiness shouldBe apiUpdate.description
      desUpdate.addressDetails.addressLine1 shouldBe apiUpdate.address.lineOne
      desUpdate.addressDetails.addressLine2 shouldBe apiUpdate.address.lineTwo
      desUpdate.addressDetails.addressLine3 shouldBe apiUpdate.address.lineThree
      desUpdate.addressDetails.addressLine4 shouldBe apiUpdate.address.lineFour
      desUpdate.addressDetails.postalCode shouldBe apiUpdate.address.postcode
      desUpdate.addressDetails.countryCode shouldBe apiUpdate.address.country
      desUpdate.contactDetails shouldBe ContactDetails.from(apiUpdate.contactDetails)
      desUpdate.paperless shouldBe apiUpdate.paperless
      desUpdate.seasonal shouldBe apiUpdate.seasonal
    }

  }

}
