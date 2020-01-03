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

package mocks.definition

import mocks.Mock
import org.scalatest.Suite
import router.definition.SelfAssessmentApiDefinition

trait MockSelfAssessmentApiDefinition extends Mock { _: Suite =>

  val mockSelfAssessmentApiDefinition = mock[SelfAssessmentApiDefinition]

  object MockSelfAssessmentApiDefinition {
    def definition = {
      when(mockSelfAssessmentApiDefinition.definition)
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSelfAssessmentApiDefinition)
  }
}
