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

package config

import java.time.Instant

import mocks.Mock
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.http.Status
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import router.errors.ErrorCode
import support.UnitSpec
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.http.{HeaderCarrier, JsValidationException, NotFoundException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

/**
  * Unit tests for [[ErrorHandler]]
  */
class ErrorHandlerSpec extends UnitSpec with Mock with GuiceOneAppPerSuite {
  implicit val erFormats: OFormat[ErrorResponse] = Json.format[ErrorResponse]

  def versionHeader(version: String): (String, String) = ACCEPT -> s"application/vnd.hmrc.$version+json"

  class Test(versionInHeader: Option[String]) {
    val method = "some-method"

    val requestHeader = FakeRequest().withHeaders(versionInHeader.map(versionHeader).toSeq: _*)

    val auditConnector = mock[AuditConnector]
    val httpAuditEvent = mock[HttpAuditEvent]

    val eventTags: Map[String, String] = Map("transactionName" -> "event.transactionName")

    def dataEvent(auditType: String) = DataEvent(
      auditSource = "auditSource",
      auditType = auditType,
      eventId = "",
      tags = eventTags,
      detail = Map("test" -> "test"),
      generatedAt = Instant.now()
    )

    def mockAuditCall(expectedAuditType: String) = {
      when(httpAuditEvent.dataEvent(ArgumentMatchers.eq(expectedAuditType), any[String](), any[RequestHeader](), any[Map[String, String]]())(any[HeaderCarrier]()))
        .thenReturn(dataEvent(expectedAuditType))
    }
    mockAuditCall("ResourceNotFound")
    mockAuditCall("ClientError")
    mockAuditCall("ServerValidationError")
    mockAuditCall("ServerInternalError")

    when(auditConnector.sendEvent(any[DataEvent]())(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(Success))

    val configuration = Configuration("appName" -> "myApp")
    val handler = new ErrorHandler(configuration, auditConnector, httpAuditEvent)
  }

  "onClientError" should {

    Seq(Some("1.0"),
      Some("8.0"),
      Some("XXX"),
      None)
      .foreach(behaveAsVersion1)

    def behaveAsVersion1(versionInHeader: Option[String]): Unit =
      "return 404 with version 1 error body" when {
        s"version header is $versionInHeader" in new Test(versionInHeader) {
          val result = handler.onClientError(requestHeader, Status.NOT_FOUND, "test")
          status(result) shouldBe Status.NOT_FOUND

          contentAsJson(result) shouldBe Json.parse(s"""{"statusCode":404,"message":"URI not found", "requested": "${requestHeader.path}"}""")

          val captor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
          verify(auditConnector, times(1)).sendEvent(captor.capture)(any[HeaderCarrier](), any[ExecutionContext]())
          captor.getValue.auditType shouldBe "ResourceNotFound"
        }
      }

    "return 404 with version 2 error body" when {
      "resource not found and version 2 header is supplied" in new Test(Some("2.0")) {
        val result = handler.onClientError(requestHeader, NOT_FOUND, "test")
        status(result) shouldBe NOT_FOUND

        contentAsJson(result) shouldBe Json.toJson(ErrorCode.matchingResourceNotFound)

        val captor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
        verify(auditConnector, times(1)).sendEvent(captor.capture)(any[HeaderCarrier](), any[ExecutionContext]())
        captor.getValue.auditType shouldBe "ResourceNotFound"
      }
    }

    "return 401 with version 2 error body" when {
      "unauthorised and version 2 header is supplied" in new Test(Some("2.0")) {
        val result = handler.onClientError(requestHeader, UNAUTHORIZED, "test")
        status(result) shouldBe UNAUTHORIZED

        contentAsJson(result) shouldBe Json.toJson(ErrorCode.unauthorisedError)

        val captor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
        verify(auditConnector, times(1)).sendEvent(captor.capture)(any[HeaderCarrier](), any[ExecutionContext]())
        captor.getValue.auditType shouldBe "ClientError"
      }
    }
  }

  "onServerError" should {

    Seq(Some("1.0"),
      Some("8.0"),
      Some("XXX"),
      None)
      .foreach(behaveAsVersion1)

    def behaveAsVersion1(versionInHeader: Option[String]): Unit =
      "return 404 with version 1 error body" when {
        s"version header is $versionInHeader" in new Test(versionInHeader) {
          val resultF = handler.onServerError(requestHeader, new NotFoundException("test") with NoStackTrace)
          status(resultF) shouldEqual NOT_FOUND
          contentAsJson(resultF) shouldEqual Json.parse("""{"statusCode":404,"message":"test"}""")

          val captor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
          verify(auditConnector, times(1)).sendEvent(captor.capture)(any[HeaderCarrier](), any[ExecutionContext]())
          captor.getValue.auditType shouldBe "ResourceNotFound"
        }
      }

    "return 404 with version 2 error body" when {
      "NotFoundException thrown and version 2 header is supplied" in new Test(Some("2.0")) {
        val result = handler.onServerError(requestHeader, new NotFoundException("test") with NoStackTrace)
        status(result) shouldBe NOT_FOUND

        contentAsJson(result) shouldBe Json.toJson(ErrorCode.matchingResourceNotFound)

        val captor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
        verify(auditConnector, times(1)).sendEvent(captor.capture)(any[HeaderCarrier](), any[ExecutionContext]())
        captor.getValue.auditType shouldBe "ResourceNotFound"
      }
    }

    "return 401 with version 2 error body" when {
      "AuthorisationException thrown and version 2 header is supplied" in new Test(Some("2.0")) {
        val result = handler.onServerError(requestHeader, new InsufficientEnrolments("test") with NoStackTrace)
        status(result) shouldBe UNAUTHORIZED

        contentAsJson(result) shouldBe Json.toJson(ErrorCode.unauthorisedError)

        val captor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
        verify(auditConnector, times(1)).sendEvent(captor.capture)(any[HeaderCarrier](), any[ExecutionContext]())
        captor.getValue.auditType shouldBe "ClientError"
      }

      "return 400 with version 2 error body" when {
        "JsValidationException thrown and version 2 header is supplied" in new Test(Some("2.0")) {
          val result = handler.onServerError(requestHeader, new JsValidationException("test", "test", classOf[String], "errs") with NoStackTrace)
          status(result) shouldBe BAD_REQUEST

          contentAsJson(result) shouldBe Json.toJson(ErrorCode.invalidRequest)

          val captor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
          verify(auditConnector, times(1)).sendEvent(captor.capture)(any[HeaderCarrier](), any[ExecutionContext]())
          captor.getValue.auditType shouldBe "ServerValidationError"
        }
      }

      "return 500 with version 2 error body" when {
        "other exeption thrown and version 2 header is supplied" in new Test(Some("2.0")) {
          val result = handler.onServerError(requestHeader, new Exception with NoStackTrace)
          status(result) shouldBe INTERNAL_SERVER_ERROR

          contentAsJson(result) shouldBe Json.toJson(ErrorCode.internalServerError)

          val captor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
          verify(auditConnector, times(1)).sendEvent(captor.capture)(any[HeaderCarrier](), any[ExecutionContext]())
          captor.getValue.auditType shouldBe "ServerInternalError"
        }
      }
    }
  }
}
