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
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, AccountingType, des}

class SelfEmploymentRetrieveSpec extends UnitSpec {
  def createDesSelfEmployment(accountingType: String = "cash"): des.SelfEmployment = {
    des.SelfEmployment(incomeSourceId = Some("abc"),
                       accountingPeriodStartDate = "2017-01-04",
                       accountingPeriodEndDate = "2017-01-05",
                       tradingName = "Acme Ltd.",
                       addressDetails = Some(
                         des.SelfEmploymentAddress(
                           addressLine1 = "1 Acme Rd.",
                           addressLine2 = Some("London"),
                           addressLine3 = Some("Greater London"),
                           addressLine4 = Some("United Kingdom"),
                           postalCode = Some("A9 9AA")
                         )),
                       typeOfBusiness = Some("Accountancy services"),
                       tradingStartDate = Some("2017-04-01"),
                       cashOrAccruals = accountingType)
  }

  "constructing a API SelfEmploymentRetrieve using the DES SelfEmployment" should {
    "correctly map fields" in {
      val selfEmployment = SelfEmploymentRetrieve.from.from(createDesSelfEmployment()).right.get

      selfEmployment.id shouldBe Some("abc")
      selfEmployment.accountingPeriod shouldBe AccountingPeriod(LocalDate.parse("2017-01-04"),
                                                                LocalDate.parse("2017-01-05"))
      selfEmployment.tradingName shouldBe "Acme Ltd."
      selfEmployment.businessAddressLineOne shouldBe Some("1 Acme Rd.")
      selfEmployment.businessAddressLineTwo shouldBe Some("London")
      selfEmployment.businessAddressLineThree shouldBe Some("Greater London")
      selfEmployment.businessAddressLineFour shouldBe Some("United Kingdom")
      selfEmployment.businessPostcode shouldBe Some("A9 9AA")
      selfEmployment.businessDescription shouldBe Some("Accountancy services")
      selfEmployment.commencementDate shouldBe Some(LocalDate.parse("2017-04-01"))
      selfEmployment.accountingType shouldBe AccountingType.CASH
      selfEmployment.cessationDate shouldBe None
    }

    "correctly map the accrual accounting type" in {
      val selfEmployment = SelfEmploymentRetrieve.from.from(createDesSelfEmployment(accountingType = "accruals")).right.get

      selfEmployment.accountingType shouldBe AccountingType.ACCRUAL
    }
  }
}
