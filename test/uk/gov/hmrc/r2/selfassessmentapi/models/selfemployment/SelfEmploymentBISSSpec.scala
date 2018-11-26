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

package uk.gov.hmrc.r2.selfassessmentapi.models.selfemployment

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class SelfEmploymentBISSSpec extends JsonSpec{

  val readJson = Json.parse("""{
                              |    "totalIncome": 100.00,
                              |    "totalExpenses": 50.00,
                              |    "totalAdditions": 5.00,
                              |    "totalDeductions": 60.00,
                              |    "accountingAdjustments": -50.00,
                              |    "netProfit": 50.00,
                              |    "taxableProfit": 0.00,
                              |    "netLoss": 0.00,
                              |    "taxableLoss": 5.00
                          }""".stripMargin)

  "SelfEmploymentBISS conversion" should {
    "return a valid result" in {
      assertJsonValidationPasses[SelfEmploymentBISS](readJson)
    }
  }
}
