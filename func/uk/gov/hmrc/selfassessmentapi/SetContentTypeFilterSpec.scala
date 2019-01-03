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

import uk.gov.hmrc.support.BaseFunctionalSpec

class SetContentTypeFilterSpec extends BaseFunctionalSpec {
  "Set Content Type filter" should {
    "set the content type of the response to application/json" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .noContentTypeFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
        .responseContainsHeader("Content-Type", "application/json".r)
    }
  }
}
