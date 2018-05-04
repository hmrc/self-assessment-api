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

package uk.gov.hmrc.selfassessmentapi.resources

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import org.mockito.Matchers
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.connectors.CharitableGivingsConnector
import uk.gov.hmrc.selfassessmentapi.models.des.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.models.{SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.{CharitableGivingsResponse, EmptyResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CharitableGivingsResourceSpec extends BaseResourceSpec {

  class Setup {
    val resource = new CharitableGivingsResource {
      override val charitableGivingsConnector = mock[CharitableGivingsConnector]
      override val appContext = mockAppContext
      override val authService = mockAuthorisationService
    }
    mockAPIAction(SourceType.CharitableGivings)

    def setUp() = {
      when(resource.charitableGivingsConnector.update(Matchers.anyObject[Nino](), Matchers.anyObject[TaxYear](), Matchers.anyObject[CharitableGivings]())
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(EmptyResponse(HttpResponse(NO_CONTENT))))
      when(resource.charitableGivingsConnector.get(Matchers.anyObject[Nino](), Matchers.anyObject[TaxYear]())
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(CharitableGivingsResponse(HttpResponse(OK, Some(Json.parse(validRequestJson))))))
    }
  }

  val validRequestWithNoContentJson =
    """
      |{
      |
      |}
    """.stripMargin

  val validRequestJson =
    """
      |{
      | "giftAidPayments": {
      |   "currentYear": 10000.32,
      |   "oneOffCurrentYear": 1000.23,
      |   "currentYearTreatedAsPreviousYear": 300.27,
      |   "nextYearTreatedAsCurrentYear": 400.13,
      |   "nonUKCharities": 2000.19
      |  },
      |  "gifts": {
      |     "landAndBuildings": 700.11,
      |     "sharesOrSecurities": 600.31,
      |     "investmentsNonUKCharities": 300.22
      |  }
      |}
    """.stripMargin


  implicit val hc = HeaderCarrier()
  implicit val system: ActorSystem = ActorSystem("PropertiesPeriodStatementResourceSpec")
  implicit val materializer: Materializer = ActorMaterializer()

  "Update charitable givings for a given tax year with invalid nino " when {
    "CharitableGivingsResource.updatePayments is called " should {
      "return IllegalArgumentException for invalid nino" in new Setup {
        assertThrows[IllegalArgumentException](resource.updatePayments(Nino("111111111"),
          TaxYear("")))
        assert(intercept[IllegalArgumentException](resource.updatePayments(Nino("111111111"),
          TaxYear(""))).getMessage === "requirement failed: 111111111 is not a valid nino.")
      }
    }
  }

  "Update charitable givings with invalid tax year and a valid nino " when {
    "CharitableGivingsResource.updatePayments is called " should {
      "return IllegalArgumentException for invalid tax year" in new Setup {
        assertThrows[IllegalArgumentException](resource.updatePayments(validNino,
          TaxYear("qwwww")))
      }
    }
  }

  "Update charitable givings for a given tax year with valid nino and no json" when {
    "CharitableGivingsResource.updatePayments is called " should {
      "successfully update the gift payments " in new Setup {
        setUp()
        submitWithSessionAndAuth(resource.updatePayments(validNino, TaxYear("2017-18")), validRequestWithNoContentJson){
          result => status(result) shouldBe NO_CONTENT
        }
      }
    }
  }

  "Update charitable givings for a given tax year with valid nino and valid json" when {
    "CharitableGivingsResource.updatePayments is called " should {
      "successfully update the gift payments " in new Setup {
        setUp()
        submitWithSessionAndAuth(resource.updatePayments(validNino, TaxYear("2017-18")), validRequestJson){
          result => status(result) shouldBe NO_CONTENT
        }
      }
    }
  }

  "Retrieve charitable givings for a given tax year with valid nino" when {
    "CharitableGivingsResource.retrievePayments is called " should {
      "successfully return the gift payments " in new Setup {
        setUp()
        showWithSessionAndAuth(resource.retrievePayments(validNino, TaxYear("2017-18"))){
          result => status(result) shouldBe OK
            result.onComplete(x => assert((contentAsJson(result) \ "giftAidPayments" \ "currentYear") ===
              Json.toJson(Json.parse(validRequestJson)) \ "giftAidPayments" \ "currentYear"))
        }
      }
    }
  }

  "Retrieve charitable givings for an invalid tax year with valid nino" when {
    "CharitableGivingsResource.retrievePayments is called " should {
      "return the invalid tax year error " in new Setup {
        when(resource.charitableGivingsConnector.get(Matchers.anyObject[Nino](), Matchers.anyObject[TaxYear]())
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(CharitableGivingsResponse(HttpResponse(BAD_REQUEST, Some(Json.toJson(Jsons.Errors.invalidTaxYear))))))
        showWithSessionAndAuth(resource.retrievePayments(validNino, TaxYear("17-18"))){
          result => status(result) shouldBe BAD_REQUEST
        }
      }
    }
  }
}
