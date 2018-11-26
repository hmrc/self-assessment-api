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

package uk.gov.hmrc.r2.selfassessmentapi.resources

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, OptionValues, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.Configuration
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, ResultExtractors}
import uk.gov.hmrc.selfassessmentapi.TestUtils
import uk.gov.hmrc.selfassessmentapi.mocks.auth.MockAuthorisationService
import uk.gov.hmrc.selfassessmentapi.mocks.config.MockAppContext
import uk.gov.hmrc.selfassessmentapi.models.SourceType.SourceType

trait ResourceSpec extends WordSpec
  with Matchers
  with MockitoSugar
  with OptionValues
  with ResultExtractors
  with HeaderNames
  with Status
  with DefaultAwaitTimeout
  with MimeTypes
  with TestUtils
  with MockAppContext
  with MockAuthorisationService
  with OneAppPerSuite {

  val nino = generateNino

  def mockAPIAction(source: SourceType,
                    featureEnabled: Boolean = true,
                    authEnabled: Boolean = false) = {
    MockAppContext.featureSwitch returns Some(Configuration(s"$source.enabled" -> featureEnabled))
    MockAppContext.authEnabled returns authEnabled
  }

  implicit class FakeRequestOps(req: FakeRequest[_]){
    // helper function to create a request when parse.empty is used in the resource
    def ignoreBody: FakeRequest[Unit] = req.withBody[Unit](())
  }
}
