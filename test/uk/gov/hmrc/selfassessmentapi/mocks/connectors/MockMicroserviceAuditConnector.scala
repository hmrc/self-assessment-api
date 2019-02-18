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

package uk.gov.hmrc.selfassessmentapi.mocks.connectors

import org.scalatest.Suite
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.selfassessmentapi.connectors.MicroserviceAuditConnector
import uk.gov.hmrc.selfassessmentapi.mocks.Mock

import scala.concurrent.{ExecutionContext, Future}

trait MockMicroserviceAuditConnector extends Mock {
  _: Suite =>

  val microserviceAuditConnector = mock[MicroserviceAuditConnector]

  object MockMicroserviceAuditConnector {

    def sendExtendedEvent(event: ExtendedDataEvent)(implicit hc: HeaderCarrier = HeaderCarrier(), ec : ExecutionContext) = {
      when(microserviceAuditConnector.sendExtendedEvent(event)(any(), any()))
    }

  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(microserviceAuditConnector)
  }
}
