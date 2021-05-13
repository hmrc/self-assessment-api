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

package router.resources

import router.errors.ErrorCode
import support.IntegrationSpec

class DeprecatedRoutesISpec extends IntegrationSpec {

  val gone = ErrorCode.resourceGone.asJson

  "hitting a deprecated route" should {
    "return 410" when {
      "the route is GET UK Property" in {
        val outgoingUrl = s"/ni/AA111111A/uk-property"

        Given()
          .theClientIsAuthorised
          .And()
          .get(outgoingUrl)
          .returns(aResponse
            .withStatus(GONE)
            .withBody(gone))
      }
    }
  }
}
