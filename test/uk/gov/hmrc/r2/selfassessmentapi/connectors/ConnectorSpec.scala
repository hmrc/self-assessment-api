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

package uk.gov.hmrc.r2.selfassessmentapi.connectors

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.r2.selfassessmentapi.mocks.MockHttp
import uk.gov.hmrc.r2.selfassessmentapi.mocks.config.MockAppContext

import scala.concurrent.ExecutionContext

trait ConnectorSpec extends UnitSpec
  with MockHttp
  with MockAppContext {

  val nino: Nino = generateNino

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext =  scala.concurrent.ExecutionContext.global

  val desToken = "test-token"
  val desEnv = "test-env"
}
