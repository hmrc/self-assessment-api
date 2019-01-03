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

package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.selfassessmentapi.resources.GovTestScenarioHeader
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.support.BaseFunctionalSpec

class ClientSubscriptionSimulationSpec extends BaseFunctionalSpec {

  "Request for self-employments with Gov-Test-Scenario = CLIENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing client should be subscribed to MTD" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders(GovTestScenarioHeader, "CLIENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.CLIENT_NOT_SUBSCRIBED.toString)
    }
  }

  "Request for dividends with Gov-Test-Scenario = CLIENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing client should be subscribed to MTD" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(s"/ni/$nino/dividends/$taxYear")
        .withHeaders(GovTestScenarioHeader, "CLIENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.CLIENT_NOT_SUBSCRIBED.toString)
    }
  }
}
