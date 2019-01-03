/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.mocks.connectors

import org.scalatest.Suite
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.connectors.PropertiesConnector
import uk.gov.hmrc.r2.selfassessmentapi.mocks.Mock
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.NewProperties

trait MockPropertiesConnector extends Mock {
  _: Suite =>

  val mockPropertiesConnector = mock[PropertiesConnector]

  object MockPropertiesConnector {
    def create(nino: Nino, newProperties: NewProperties) = {
      when(mockPropertiesConnector.create(eqTo(nino), eqTo(newProperties))(any(), any()))
    }

    def retrieve(nino: Nino) = {
      when(mockPropertiesConnector.retrieve(eqTo(nino))(any(), any()))
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockPropertiesConnector)
  }
}
