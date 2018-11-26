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

package uk.gov.hmrc.r1.selfassessmentapi.resources

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.JsObject
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.r2.selfassessmentapi.connectors.PropertiesPeriodStatementConnector
import uk.gov.hmrc.r2.selfassessmentapi.models.{Errors, Period, SourceType}
import uk.gov.hmrc.r2.selfassessmentapi.models.audit.EndOfPeriodStatementDeclaration
import uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers.EmptyResponse
import uk.gov.hmrc.r2.selfassessmentapi.services.{AuditData, AuditService}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PropertiesPeriodStatementResourceSpec extends BaseResourceSpec {

  object TestResource extends PropertiesPeriodStatementResource {
    override lazy val statementConnector = mock[PropertiesPeriodStatementConnector]
    override lazy val auditService = mock[AuditService]
    override val appContext = mockAppContext
    override val authService = mockAuthorisationService
    mockAPIAction(SourceType.Properties)
  }

  val requestJson =
    """
      |{
      |"finalised": true
      |}
    """.stripMargin

  val invalidRequestJson =
    """
      |{
      |"finalised": false
      |}
    """.stripMargin

  val invalidNullRequestJson =
    """
      |{
      |"finalised": null
      |}
    """.stripMargin

  implicit val hc = HeaderCarrier()
  implicit val system: ActorSystem = ActorSystem("PropertiesPeriodStatementResourceSpec")
  implicit val materializer: Materializer = ActorMaterializer()

  def setUp() {
    when(TestResource.auditService.audit(Matchers.anyObject[AuditData[EndOfPeriodStatementDeclaration]]())
    (Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
  }

  "Submit UK property end of period statements with valid nino, from and to dates, declaration flag " when {

    "PropertiesPeriodStatementResource.finaliseEndOfPeriodStatement is called" should {
      "return successful submission response with no content" in {
        setUp()
        when(TestResource.statementConnector.create(Matchers.anyObject[Nino](), Matchers.anyObject[Period](), Matchers.anyObject[String]())
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(EmptyResponse(HttpResponse(NO_CONTENT))))
        submitWithSessionAndAuth(TestResource.finaliseEndOfPeriodStatement(validNino,
          DateTime.now().minusDays(1).toLocalDate, DateTime.now().toLocalDate),requestJson){
          result => status(result) shouldBe NO_CONTENT
        }
      }
    }
  }

  "Submit UK property end of period statements with invalid nino " when {

    "PropertiesPeriodStatementResource.finaliseEndOfPeriodStatement is called" should {
      "return IllegalArgumentException for the nino" in {
        setUp()
        assertThrows[IllegalArgumentException](TestResource.finaliseEndOfPeriodStatement(Nino("111111111"),
                  DateTime.now().minusDays(1).toLocalDate, DateTime.now().toLocalDate))

        assert(intercept[IllegalArgumentException](TestResource.finaliseEndOfPeriodStatement(Nino("111111111"),
          DateTime.now().minusDays(1).toLocalDate, DateTime.now().toLocalDate)).getMessage === "requirement failed: 111111111 is not a valid nino.")
      }
    }
  }

  "Submit UK property end of period statements with valid nino, and invalid from (start) date " when {

    "PropertiesPeriodStatementResource.finaliseEndOfPeriodStatement is called" should {
      "return invalid start date error response" in {
        setUp()
        submitWithSessionAndAuth(TestResource.finaliseEndOfPeriodStatement(validNino,
          DateTime.now().minusYears(2).toLocalDate, DateTime.now().toLocalDate), requestJson) {
          result => status(result) shouldBe BAD_REQUEST
            result.onComplete(x => assert(Errors.InvalidStartDate.code === (contentAsJson(result) \ "code").as[String]))
        }
      }
    }
  }

  "Submit UK property end of period statements with valid nino, and invalid accounting period " when {

    "PropertiesPeriodStatementResource.finaliseEndOfPeriodStatement is called" should {
      "return invalid accounting period error response" in {
        setUp()
        submitWithSessionAndAuth(TestResource.finaliseEndOfPeriodStatement(validNino,
          DateTime.now().toLocalDate, DateTime.now().minusDays(2).toLocalDate), requestJson) {
          result => status(result) shouldBe BAD_REQUEST
            result.onComplete(x => assert(Errors.InvalidDateRange.code === (contentAsJson(result) \ "code").as[String]))
        }
      }
    }
  }

  "Submit UK property end of period statements with valid nino, accounting period and invalid declaration" when {

    "PropertiesPeriodStatementResource.finaliseEndOfPeriodStatement is called" should {
      "return not finalised declaration error response" in {
        setUp()
        submitWithSessionAndAuth(TestResource.finaliseEndOfPeriodStatement(validNino,
          DateTime.now().minusDays(1).toLocalDate, DateTime.now().toLocalDate), invalidRequestJson) {
          result => status(result) shouldBe FORBIDDEN
            result.onComplete(x => assert(Errors.NotFinalisedDeclaration.code === ((contentAsJson(result) \ "errors").as[Seq[JsObject]].head \ "code").as[String]))
        }
      }
    }
  }

  "Submit UK property end of period statements with valid nino, accounting period and declaration with missing updates" when {

    "PropertiesPeriodStatementResource.finaliseEndOfPeriodStatement is called" should {
      "return missing periodic updates error response" in {
        when(TestResource.statementConnector.create(Matchers.anyObject[Nino](), Matchers.anyObject[Period](), Matchers.anyObject[String]())
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(EmptyResponse(HttpResponse(FORBIDDEN))))
        submitWithSessionAndAuth(TestResource.finaliseEndOfPeriodStatement(validNino,
          DateTime.now().minusDays(1).toLocalDate, DateTime.now().toLocalDate), invalidRequestJson) {
          result => status(result) shouldBe FORBIDDEN
        }
      }
    }
  }

  "Submit UK property end of period statements with valid nino, accounting period and no declaration" when {

    "PropertiesPeriodStatementResource.finaliseEndOfPeriodStatement is called" should {
      "return not finalised declaration error response" in {
        setUp()
        submitWithSessionAndAuth(TestResource.finaliseEndOfPeriodStatement(validNino,
          DateTime.now().minusDays(1).toLocalDate, DateTime.now().toLocalDate), invalidNullRequestJson) {
          result => status(result) shouldBe BAD_REQUEST
            result.onComplete(x => assert("INVALID_BOOLEAN_VALUE" === ((contentAsJson(result) \ "errors").as[Seq[JsObject]].head \ "code").as[String]))
        }
      }
    }
  }
}
