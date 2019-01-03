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

package uk.gov.hmrc.selfassessmentapi.featureswitch

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentPeriodicSummaryFeatureSwitchSpec extends BaseFunctionalSpec {

  private val conf: Map[String, _] =
    Map("Test" ->
      Map("feature-switch" ->
        Map("self-employments" ->
          Map("enabled" -> true, "annual" -> Map("enabled" -> true), "periods" -> Map("enabled" -> false))
        )
      )
    )

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "self-employments" should {
    "not be visible if feature Switched Off" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(404)
    }
  }

}


