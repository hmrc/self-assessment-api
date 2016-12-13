/*
 * Copyright 2016 HM Revenue & Customs
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

import org.scalatest.Matchers
import uk.gov.hmrc.play.auth.controllers.AuthConfig
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel.L50
import uk.gov.hmrc.play.auth.microservice.connectors.{AuthRequestParameters, HttpVerb}
import uk.gov.hmrc.selfassessmentapi.config.MicroserviceAuthFilter
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator

class MicroserviceAuthFilterSpec extends UnitSpec with Matchers {

  val underTest = MicroserviceAuthFilter

  "MicroserviceAuthFilter" should {
    val nino = NinoGenerator().nextNino()
    "extract resource that builds valid auth url" in {
      val resource = underTest.extractResource(s"/$nino/employments", HttpVerb("GET"), AuthConfig(pattern = "/(\\w+)/.*".r, confidenceLevel = L50))
      resource.get.buildUrl("http://authhost.com/auth", AuthRequestParameters(L50)) shouldBe s"http://authhost.com/auth/authorise/read/paye/$nino?confidenceLevel=50"
    }
  }
}
