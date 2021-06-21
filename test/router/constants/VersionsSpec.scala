/*
 * Copyright 2021 HM Revenue & Customs
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

package router.constants

import router.constants.Versions.AcceptHeader
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

class VersionsSpec extends UnitSpec {

  "getAPIVersionFromRequest" should {
    Seq(
      "application/vnd.hmrc.1.0+json" -> "1.0",
      "application/vnd.hmrc.0.0+json" -> "0.0",
      "application/vnd.hmrc.9.9+json" -> "9.9"
    ).foreach {
      case (headerValue, version) =>
        "extract and return the version number from the Accept header" when {
          s"the Accept header contains a value of $headerValue" in {
            val hc = HeaderCarrier(extraHeaders = Seq((ACCEPT, headerValue)))
            Versions.getAPIVersionFromRequest(hc) shouldBe Some(version)
          }
        }
    }

    Seq(
      "test incorrect value",
      "application/vnd.hmrc.a.1+json",
      "application/vnd.hmrc.1+json",
      "application/vnd.hmrc.10+json",
      "application/vnd.hmrc.-1.0+json",
      "application/vnd.hmrc.10.0+json"
    ).foreach { headerValue =>
      "return a None" when {
        s"the Accept header contains an incorrect value of $headerValue" in {
          val hc = HeaderCarrier(extraHeaders = Seq((ACCEPT, headerValue)))
          Versions.getAPIVersionFromRequest(hc) shouldBe None
        }
      }
    }
  }

  "extractAcceptHeader" should {
    "return a header" when {
      "an Accept header is provided in extraHeaders" in {
        val hc = HeaderCarrier(extraHeaders = Seq((ACCEPT, "application/vnd.hmrc.1.0+json")))
        Versions.extractAcceptHeader(hc) shouldBe Some(AcceptHeader("1.0", "json"))
      }
      "an Accept header is provided in otherHeaders" in {
        val hc = HeaderCarrier(otherHeaders = Seq((ACCEPT, "application/vnd.hmrc.1.0+json")))
        Versions.extractAcceptHeader(hc) shouldBe Some(AcceptHeader("1.0", "json"))
      }
    }
    "return None" when {
      "no Accept header is provided" in {
        val hc = HeaderCarrier()
        Versions.extractAcceptHeader(hc) shouldBe None
      }
    }
  }

  "convertHeaderToVersion1" should {
    "return a version 1 header" when {
      "a non version 1 header is supplied" in {
        val hc = HeaderCarrier(
          otherHeaders = Seq(ACCEPT  -> "application/vnd.hmrc.2.0+json", "test" -> "header"),
          extraHeaders = Seq("other" -> "test-header")
        )
        Versions.convertHeaderToVersion1(hc).headers(Seq(ACCEPT)) shouldBe Seq(ACCEPT -> "application/vnd.hmrc.1.0+json")
      }
    }
  }
}
