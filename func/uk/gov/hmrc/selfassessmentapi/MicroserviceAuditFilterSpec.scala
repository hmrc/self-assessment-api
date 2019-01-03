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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{urlPathEqualTo, _}
import org.scalatest.time.{Millis, Seconds, Span}
import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class MicroserviceAuditFilterSpec extends BaseFunctionalSpec {

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  "Audit filter" should {
    "be applied when a POST request is made" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)

      eventually {
        WireMock.findAll(postRequestedFor(urlPathEqualTo("/write/audit"))
        ).size shouldBe 1
      }
    }
  }

}
