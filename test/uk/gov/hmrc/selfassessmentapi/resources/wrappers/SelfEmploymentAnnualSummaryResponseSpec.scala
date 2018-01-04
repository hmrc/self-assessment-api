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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.http.HttpResponse

class SelfEmploymentAnnualSummaryResponseSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {
  private val mockResponse = mock[HttpResponse]
  private val unitUnderTest = SelfEmploymentAnnualSummaryResponse(mockResponse)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockResponse)
  }

  "annualSummary" should {
    "return Some(apiSummary) if the response from DES is correct" in {
      when(mockResponse.json).thenReturn(Json.obj())

      unitUnderTest.annualSummary.isDefined shouldBe true
    }
  }
}
