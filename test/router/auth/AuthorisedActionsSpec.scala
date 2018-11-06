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

package router.auth

import mocks.auth.MockAuthConnector
import support.UnitSpec
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import play.api.mvc.Results._
import play.api.test.FakeRequest
import router.errors.ErrorCode
import uk.gov.hmrc.auth.core.{InternalError, InvalidBearerToken}

import scala.concurrent.Future

class AuthorisedActionsSpec extends UnitSpec
  with MockAuthConnector {

  class Setup {
    val authorisedActions = new AuthorisedActions with BaseController {
      override val authConnector = mockAuthConnector
    }
  }

  "AuthAction" should {
    "return a 200" when {
      "the request is authorised" in new Setup {
        MockAuthConnector.authorise() returns Future.successful(())

        val result = authorisedActions.AuthAction(_ => Ok)(FakeRequest())
        status(result) shouldBe OK
      }
    }

    "return a 403" when {
      "the auth connector throws an InvalidBearerToken exception" in new Setup {
        MockAuthConnector.authorise() returns Future.failed(InvalidBearerToken())

        val result = authorisedActions.AuthAction(_ => Ok)(FakeRequest())
        status(result) shouldBe FORBIDDEN
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.invalidBearerToken.asJson
      }
    }

    "return a 403" when {
      "the auth connector throws any other AuthorisationException exception" in new Setup {
        MockAuthConnector.authorise() returns Future.failed(InternalError())

        val result = authorisedActions.AuthAction(_ => Ok)(FakeRequest())
        status(result) shouldBe FORBIDDEN
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.unauthorisedError.asJson
      }
    }
  }
}
