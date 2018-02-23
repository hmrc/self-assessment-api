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
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.connectors.GiftAidPaymentsConnector
import uk.gov.hmrc.selfassessmentapi.models.des.giftaid.GiftAidPayments
import uk.gov.hmrc.selfassessmentapi.models.{Errors, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.EmptyResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GiftAidPaymentsResourceSpec extends BaseResourceSpec {

  object TestResource extends GiftAidPaymentsResource {
    override lazy val giftAidPaymentsConnector: GiftAidPaymentsConnector = mock[GiftAidPaymentsConnector]
  }

  val invalidRequestJson =
    """
      |{
      |
      |}
    """.stripMargin

  val validRequestJson =
    """
      |{
      | "totalPayments": 613.99,
      |  "totalOneOffPayments": 13.99,
      |  "totalPaymentsBeforeTaxYearStart": 120.00,
      |  "totalPaymentsAfterTaxYearEnd": 82.00,
      |  "sharesOrSecurities": 300.00,
      |  "ukCharityGift": {
      |    "landAndBuildings": 400.00
      |  },
      |  "nonUKCharityGift": {
      |    "investments": 800.00,
      |    "payments": 500.00
      |  }
      |}
    """.stripMargin

  val invalidUKCharityRequestJson =
    """
      |{
      | "totalPayments": 613.99,
      |  "totalOneOffPayments": 13.99,
      |  "totalPaymentsBeforeTaxYearStart": 120.00,
      |  "totalPaymentsAfterTaxYearEnd": 82.00,
      |  "sharesOrSecurities": 300.00,
      |  "ukCharityGift": {
      |  },
      |  "nonUKCharityGift": {
      |    "investments": 800.00,
      |    "payments": 500.00
      |  }
      |}
    """.stripMargin

  val invalidNonUkCharityRequestJson =
    """
      |{
      | "totalPayments": 613.99,
      |  "totalOneOffPayments": 13.99,
      |  "totalPaymentsBeforeTaxYearStart": 120.00,
      |  "totalPaymentsAfterTaxYearEnd": 82.00,
      |  "sharesOrSecurities": 300.00,
      |  "ukCharityGift": {
      |   "landAndBuildings": 400.00
      |  },
      |  "nonUKCharityGift": {
      |  }
      |}
    """.stripMargin

  val validRequestWithoutUKCharityJson =
    """
      |{
      | "totalPayments": 613.99,
      |  "totalOneOffPayments": 13.99,
      |  "totalPaymentsBeforeTaxYearStart": 120.00,
      |  "totalPaymentsAfterTaxYearEnd": 82.00,
      |  "sharesOrSecurities": 300.00,
      |  "nonUKCharityGift": {
      |    "investments": 800.00,
      |    "payments": 500.00
      |  }
      |}
    """.stripMargin

  val validRequestWithoutNonUKCharityJson =
    """
      |{
      | "totalPayments": 613.99,
      |  "totalOneOffPayments": 13.99,
      |  "totalPaymentsBeforeTaxYearStart": 120.00,
      |  "totalPaymentsAfterTaxYearEnd": 82.00,
      |  "sharesOrSecurities": 300.00,
      |  "ukCharityGift": {
      |    "landAndBuildings": 400.00
      |  }
      |}
    """.stripMargin

  val validRequestWithoutTotalPaymentsJson =
    """
      |{
      |  "totalOneOffPayments": 13.99,
      |  "totalPaymentsBeforeTaxYearStart": 120.00,
      |  "totalPaymentsAfterTaxYearEnd": 82.00,
      |  "sharesOrSecurities": 300.00,
      |  "ukCharityGift": {
      |    "landAndBuildings": 400.00
      |  }
      |}
    """.stripMargin

  val validRequestLessTotalPaymentsJson =
    """
      |{
      | "totalPayments": 413.99,
      |  "totalOneOffPayments": 13.99,
      |  "totalPaymentsBeforeTaxYearStart": 120.00,
      |  "totalPaymentsAfterTaxYearEnd": 82.00,
      |  "sharesOrSecurities": 300.00,
      |  "nonUKCharityGift": {
      |    "investments": 800.00,
      |    "payments": 500.00
      |  }
      |}
    """.stripMargin

  implicit val hc = HeaderCarrier()
  implicit val system: ActorSystem = ActorSystem("PropertiesPeriodStatementResourceSpec")
  implicit val materializer: Materializer = ActorMaterializer()

  def setUp() = {
    when(TestResource.giftAidPaymentsConnector.update(Matchers.anyObject[Nino](), Matchers.anyObject[TaxYear](), Matchers.anyObject[GiftAidPayments]())
    (Matchers.any(), Matchers.any())).thenReturn(Future.successful(EmptyResponse(HttpResponse(NO_CONTENT))))
  }

  "Update gift aid payments for a given tax year with invalid nino " when {
    "GiftAidPaymentsResource.updatePayments is called " should {
      "return IllegalArgumentException for invalid nino" in {
        assertThrows[IllegalArgumentException](TestResource.updatePayments(Nino("111111111"),
          TaxYear("")))
        assert(intercept[IllegalArgumentException](TestResource.updatePayments(Nino("111111111"),
          TaxYear(""))).getMessage === "requirement failed: 111111111 is not a valid nino.")
      }
    }
  }

  "Update gift aid payments for a given tax year with valid nino and invalid json" when {
    "GiftAidPaymentsResource.updatePayments is called " should {
      "successfully update the gift payments " in {
        submitWithSessionAndAuth(TestResource.updatePayments(validNino, TaxYear("2017-18")), invalidRequestJson){
          result => status(result) shouldBe BAD_REQUEST
        }
      }
    }
  }

  /*
  @TODO once we implement the tax year validation
  "Update gift aid payments with valid nino and invalid tax year" when {
    "GiftAidPaymentsResource.updatePayments is called " should {
      "successfully update the gift payments " in {
        submitWithSessionAndAuth(TestResource.updatePayments(validNino, TaxYear("17-18")), validRequestJson){
          result => status(result) shouldBe BAD_REQUEST
        }
      }
    }
  }*/

  "Update gift aid payments for a given tax year with valid nino and invalid UK charity json" when {
    "GiftAidPaymentsResource.updatePayments is called " should {
      "successfully update the gift payments " in {
        submitWithSessionAndAuth(TestResource.updatePayments(validNino, TaxYear("2017-18")), invalidUKCharityRequestJson){
          result => status(result) shouldBe BAD_REQUEST
        }
      }
    }
  }

  "Update gift aid payments for a given tax year with valid nino and invalid Non UK charity json" when {
    "GiftAidPaymentsResource.updatePayments is called " should {
      "successfully update the gift payments " in {
        submitWithSessionAndAuth(TestResource.updatePayments(validNino, TaxYear("2017-18")), invalidNonUkCharityRequestJson){
          result => status(result) shouldBe BAD_REQUEST
        }
      }
    }
  }

  "Update gift aid payments for a given tax year with valid nino and valid json" when {
    "GiftAidPaymentsResource.updatePayments is called " should {
      "successfully update the gift payments " in {
        setUp()
        submitWithSessionAndAuth(TestResource.updatePayments(validNino, TaxYear("2017-18")), validRequestJson){
          result => status(result) shouldBe NO_CONTENT
        }
      }
    }
  }

  "Update gift aid payments for a given tax year with valid nino and valid json with out UK charity payments" when {
    "GiftAidPaymentsResource.updatePayments is called " should {
      "successfully update the gift payments " in {
        setUp()
        submitWithSessionAndAuth(TestResource.updatePayments(validNino, TaxYear("2017-18")), validRequestWithoutUKCharityJson){
          result => status(result) shouldBe NO_CONTENT
        }
      }
    }
  }

  "Update gift aid payments for a given tax year with valid nino and valid json with out Non UK charity payments" when {
    "GiftAidPaymentsResource.updatePayments is called " should {
      "successfully update the gift payments " in {
        setUp()
        submitWithSessionAndAuth(TestResource.updatePayments(validNino, TaxYear("2017-18")), validRequestWithoutNonUKCharityJson){
          result => status(result) shouldBe NO_CONTENT
        }
      }
    }
  }

  "Update gift aid payments for a given tax year with valid nino and valid json with out total payments" when {
    "GiftAidPaymentsResource.updatePayments is called " should {
      "successfully update the gift payments " in {
        submitWithSessionAndAuth(TestResource.updatePayments(validNino, TaxYear("2017-18")), validRequestWithoutTotalPaymentsJson){
          result => status(result) shouldBe BAD_REQUEST
        }
      }
    }
  }

  "Update gift aid payments for a given tax year with valid nino and valid json with less total payments than required" when {
    "GiftAidPaymentsResource.updatePayments is called " should {
      "successfully update the gift payments " in {
        setUp()
        submitWithSessionAndAuth(TestResource.updatePayments(validNino, TaxYear("2017-18")), validRequestLessTotalPaymentsJson){
          result => status(result) shouldBe BAD_REQUEST
        }
      }
    }
  }
}
