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

import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.models.Class4NicsExemptionCode.NON_RESIDENT
import uk.gov.hmrc.selfassessmentapi.models.des.selfemployment.AnnualNonFinancials

class NonFinancialsSpec extends UnitSpec {
  "from" should {
    "correctly map des AnnualNonFinancials to API NonFinancials" in {
      val desNonFinancials = Some(AnnualNonFinancials(None, Some(true), Some(true), Some("001")))

      val nonFinancials = NonFinancials.from(desNonFinancials)

      nonFinancials.get.class4NicInfo.get.isExempt shouldBe Some(true)
      nonFinancials.get.class4NicInfo.get.exemptionCode shouldBe Some(NON_RESIDENT)
      nonFinancials.get.payVoluntaryClass2Nic shouldBe Some(true)
    }

    "correctly map class4NicInfo from des AnnualNonFinancials to API NonFinancials" in {
      val desNonFinancials = Some(AnnualNonFinancials(None, Some(true), Some(false), None))

      val nonFinancials = NonFinancials.from(desNonFinancials)

      nonFinancials.get.class4NicInfo.get.isExempt shouldBe Some(false)
      nonFinancials.get.class4NicInfo.get.exemptionCode shouldBe None
      nonFinancials.get.payVoluntaryClass2Nic shouldBe Some(true)
    }


    "return None for class4NicInfo when both the class4Nic data are None" in {
      val desNonFinancials = Some(AnnualNonFinancials(None, Some(true), None, None))

      val nonFinancials = NonFinancials.from(desNonFinancials)

      nonFinancials.get.class4NicInfo shouldBe None
      nonFinancials.get.payVoluntaryClass2Nic shouldBe Some(true)
    }
  }
}
