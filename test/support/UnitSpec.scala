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

import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.mvc.ControllerComponents
import play.api.test.Helpers.stubControllerComponents
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, ResultExtractors}
import uk.gov.hmrc.http.HeaderCarrier

import scala.util.control.NoStackTrace

trait UnitSpec extends AnyWordSpecLike
  with EitherValues
  with Matchers
  with FutureAwaits
  with DefaultAwaitTimeout
  with ResultExtractors
  with HeaderNames
  with Status
  with MimeTypes {

  lazy val controllerComponents: ControllerComponents = stubControllerComponents()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val testException: Throwable = new NoStackTrace {
    override def getMessage: String = "A test exception was thrown"
  }
}
