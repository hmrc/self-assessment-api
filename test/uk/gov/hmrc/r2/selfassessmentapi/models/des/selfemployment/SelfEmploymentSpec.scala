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

package uk.gov.hmrc.r2.selfassessmentapi.models.des.selfemployment

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.AccountingType.AccountingType
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, AccountingType}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class SelfEmploymentSpec extends JsonSpec {
  def apiSelfEmployment(accountingType: AccountingType): models.selfemployment.SelfEmployment = {
    models.selfemployment.SelfEmployment(
      id = None,
      accountingPeriod = AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")),
      accountingType = accountingType,
      commencementDate = LocalDate.parse("2017-04-01"),
      cessationDate = None,
      tradingName = "Acme Ltd.",
      businessDescription = "Accountancy services",
      businessAddressLineOne = "Acme Rd.",
      businessAddressLineTwo = Some("London"),
      businessAddressLineThree = Some("Greater London"),
      businessAddressLineFour = Some("United Kingdom"),
      businessPostcode = "A9 9AA")
  }

  "constructing a DES SelfEmployment using our API SelfEmployment" should {
    "correctly map fields" in {
      val desSelfEmployment =
        Business.from(apiSelfEmployment(accountingType = AccountingType.CASH)).businessDetails.head

      desSelfEmployment.accountingPeriodStartDate shouldBe "2017-04-01"
      desSelfEmployment.accountingPeriodEndDate shouldBe "2017-04-02"
      desSelfEmployment.tradingName shouldBe "Acme Ltd."
      desSelfEmployment.addressDetails.get.addressLine1 shouldBe "Acme Rd."
      desSelfEmployment.addressDetails.get.addressLine2 shouldBe Some("London")
      desSelfEmployment.addressDetails.get.addressLine3 shouldBe Some("Greater London")
      desSelfEmployment.addressDetails.get.addressLine4 shouldBe Some("United Kingdom")
      desSelfEmployment.addressDetails.get.countryCode shouldBe "GB"
      desSelfEmployment.addressDetails.get.postalCode shouldBe Some("A9 9AA")
      desSelfEmployment.typeOfBusiness shouldBe Some("Accountancy services")
      desSelfEmployment.cashOrAccruals shouldBe "cash"
      desSelfEmployment.tradingStartDate shouldBe Some("2017-04-01")
    }

    "correctly map the accrual accounting type" in {
      val desBusiness = Business.from(apiSelfEmployment(accountingType = AccountingType.ACCRUAL))

      desBusiness.businessDetails.head.cashOrAccruals shouldBe "accruals"
    }
  }
}
