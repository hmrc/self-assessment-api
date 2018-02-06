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

package uk.gov.hmrc.selfassessmentapi.resources

import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

trait BaseResourceSpec extends UnitSpec with MockitoSugar with BeforeAndAfter with OneAppPerSuite{

  val validNino = Nino("AA123456A")

  def submitWithSessionAndAuth(action: Action[JsValue], requestJson: String)(test: Future[Result] => Any) {
    val result = action.apply(FakeRequest().withBody(Json.parse(requestJson)))
    test(result)
  }
}
