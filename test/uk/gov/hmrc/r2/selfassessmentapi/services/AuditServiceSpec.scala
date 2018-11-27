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

package uk.gov.hmrc.r2.selfassessmentapi.services

import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.selfassessmentapi.AsyncUnitSpec
import uk.gov.hmrc.r2.selfassessmentapi.models.audit.PeriodicUpdate

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends AsyncUnitSpec {

  private implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("abcd")))

  private class TestAuditService extends AuditService

  val auditPayload = PeriodicUpdate(
    auditType = "submitPeriodicUpdate",
    httpStatus = 200,
    nino = generateNino,
    sourceId = "abc",
    periodId = "def",
    affinityGroup = "individual",
    agentCode = None,
    transactionReference = Some("ghi"),
    requestPayload = Json.obj(),
    responsePayload = Some(Json.obj())
  )

  val event = ExtendedDataEvent(
    auditSource = "self-assessment-api",
    auditType = auditPayload.auditType,
    tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags("jkl", "path"),
    detail = Json.toJson(auditPayload),
    eventId = "someId",
    generatedAt = DateTime.now()
  )

  "sendEvent" should {

    "return Success if the audit was successful" in {
      val testService = new TestAuditService
      val connector = new AuditConnector {
        override def auditingConfig: AuditingConfig = ???

        override def sendExtendedEvent(event: ExtendedDataEvent)(implicit hc: HeaderCarrier,
                                                  ec: ExecutionContext): Future[AuditResult] =
          Future.successful(Success)
      }

      testService.sendEvent(event, connector) map { auditResult =>
        assert(auditResult == Success)
      }
    }

    "return Failure if an exception occurred when sending the audit event" in {
      val testService = new TestAuditService
      val ex = new RuntimeException("some non-fatal exception")
      val connector = new AuditConnector {
        override def auditingConfig: AuditingConfig = ???

        override def sendExtendedEvent(event: ExtendedDataEvent)(implicit hc: HeaderCarrier,
                                                  ec: ExecutionContext): Future[AuditResult] =
          throw ex
      }

      testService.sendEvent(event, connector) map { auditResult =>
        assert(auditResult.asInstanceOf[Failure].nested.contains(ex))
      }
    }
  }

}
