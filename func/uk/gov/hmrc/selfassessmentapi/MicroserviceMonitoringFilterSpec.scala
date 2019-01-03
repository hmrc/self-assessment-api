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

import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class MicroserviceMonitoringFilterSpec extends BaseFunctionalSpec {

  "Monitoring filter" should {
    "be applied when requests are made" in {
      given()
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeCreatedFor(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .when()
        .get("/admin/metrics")
        .thenAssertThat()
        .body(_ \ "timers" \ "Timer-API-SelfEmployments-POST" \ "count").is(1)
        .body(_ \ "timers" \ "Timer-API-SelfEmployments-GET" \ "count").is(1)
    }
  }

}
