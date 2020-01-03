/*
 * Copyright 2020 HM Revenue & Customs
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

package support

import mocks.auth.MockAuthConnector
import org.mockito.stubbing.OngoingStubbing
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval

import scala.concurrent.Future

trait ResourceSpec extends UnitSpec
  with MockAuthConnector {

  val requestJson: JsObject = Json.obj("test" -> "request json")
  val responseJson: JsObject = Json.obj("test" -> "response json")
  val testHeader = Map("test" -> "header",  "X-Content-Type-Options" -> "nosniff", "Content-Type" -> "application/json")
  val testHeaderResponse = Map("test" -> Seq("header"))

  def mockAuthAction: OngoingStubbing[Future[Unit]] = {
    MockAuthConnector.authorise(EmptyPredicate, EmptyRetrieval)
      .returns(Future.successful(()))
  }
}
