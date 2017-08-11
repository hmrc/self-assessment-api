/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.services

import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.Authorization
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.models.audit.PeriodicUpdate

import scala.concurrent.ExecutionContext

class AuditServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  "audit" should {
    "send an audit event with the provided information when invoked" in {
      val testService = new TestAuditService

      val auditPayload = PeriodicUpdate(
        httpStatus = 200,
        nino = generateNino,
        sourceId = "abc",
        periodId = "def",
        affinityGroup = "individual",
        transactionReference = Some("ghi"),
        requestPayload = Json.obj(),
        responsePayload = Some(Json.obj())
      )

      when(mockRequest.path).thenReturn("path")

      testService.audit(AuditData(detail = auditPayload, transactionName = "jkl"))

      val captor = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
      verify(mockAuditConnector).sendEvent(captor.capture)(any[HeaderCarrier], any[ExecutionContext])

      val event = captor.getValue

      event shouldBe ExtendedDataEvent(
        auditSource = "self-assessment-api",
        auditType = auditPayload.auditType,
        tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags("jkl", "path"),
        detail = Json.toJson(auditPayload),
        eventId = event.eventId,
        generatedAt = event.generatedAt
      )
    }
  }

  override protected def beforeEach(): Unit = {
    reset(mockAuditConnector, mockRequest)
  }

  private implicit val hc = HeaderCarrier(authorization = Some(Authorization("abcd")))
  private implicit val mockRequest = mock[Request[_]]
  private val mockAuditConnector = mock[AuditConnector]

  private class TestAuditService extends AuditService {
    override val auditConnector: AuditConnector = mockAuditConnector
  }
}
