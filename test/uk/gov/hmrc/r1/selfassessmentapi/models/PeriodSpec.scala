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

package uk.gov.hmrc.r1.selfassessmentapi.models

import org.joda.time.LocalDate
import uk.gov.hmrc.r2.selfassessmentapi.UnitSpec

class PeriodSpec extends UnitSpec {
  "createPeriodId" should {
    "concatenate the from and to dates separated by an underscore" in {
      (new Period {
        override val from: LocalDate = LocalDate.parse("2017-04-07")
        override val to: LocalDate = LocalDate.parse("2018-04-07")
      } periodId) shouldBe "2017-04-07_2018-04-07"
    }
  }
}
