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

package uk.gov.hmrc.selfassessmentapi.models.obligations

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import uk.gov.hmrc.selfassessmentapi.UnitSpec

class ObligationsQueryParamsSpec extends UnitSpec {
  "ObligationsQueryParamsSpec#apply" should {

      val obligationsQueryParams = ObligationsQueryParams.apply()
      val dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")
      val todaysDate = LocalDate.now()

      "return a correctly formatted 'YYYY-MM-dd' from date 366 days from todays date" in {

        val actualStringFormattedFromDate = obligationsQueryParams.from.toString
        val expectedStringFormttedFromDate = todaysDate.minusDays(366).format(dateTimeFormatter)

        actualStringFormattedFromDate shouldBe expectedStringFormttedFromDate
      }

      "return a correctly formatted 'YYYY-MM-dd' to date of todays date" in {

        val actualStringFormattedToDate = obligationsQueryParams.to.toString
        val expectedStringFormttedToDate = todaysDate.format(dateTimeFormatter)

        actualStringFormattedToDate shouldBe expectedStringFormttedToDate
      }
    }
}
