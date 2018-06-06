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

package support

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


import org.scalatest.{EitherValues, Matchers, WordSpecLike}
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, ResultExtractors}
import uk.gov.hmrc.http.HeaderCarrier

trait UnitSpec extends WordSpecLike
  with EitherValues
  with Matchers
  with FutureAwaits
  with DefaultAwaitTimeout
  with ResultExtractors
  with HeaderNames
  with Status
  with MimeTypes {

  implicit val hc: HeaderCarrier = HeaderCarrier()
}
