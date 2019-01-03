/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models

import uk.gov.hmrc.selfassessmentapi.UnitSpec

class TaxYearSpec extends UnitSpec {
  "validateFormat" should {
    "reject any tax year with an invalid format" in {
      TaxYear.createTaxYear("cake") shouldBe None
      TaxYear.createTaxYear("201617") shouldBe None
      TaxYear.createTaxYear("2116-17") shouldBe None
      TaxYear.createTaxYear("17-2016") shouldBe None
      TaxYear.createTaxYear("2099-00") shouldBe None
    }

    "reject a tax year where the gap between the start and end of a tax year is not equal to one year" in {
      TaxYear.createTaxYear("2017-19") shouldBe None
    }

    "reject a tax year before 2017-18" in {
      TaxYear.createTaxYear("2016-17") shouldBe None
    }

    "accept a tax year where the the start is before the end" in {
      TaxYear.createTaxYear("2018-17") shouldBe None
    }

    "accept a tax year with a valid format" in {
      TaxYear.createTaxYear("2017-18") shouldBe Some(TaxYear("2017-18"))
      TaxYear.createTaxYear("2098-99") shouldBe Some(TaxYear("2098-99"))
    }

    "DES tax year should be the year after the dash" in {
      TaxYear.createTaxYear("2017-18").foreach(_.toDesTaxYear shouldBe "2018")
    }
  }
}
