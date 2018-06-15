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

///*
// * Copyright 2018 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package router.services
//
//import mocks.connectors.MockSelfAssessmentConnector
//import play.api.libs.json.Json
//import play.api.test.FakeRequest
//import router.errors.{IncorrectAPIVersion, UnsupportedAPIVersion}
//import support.UnitSpec
//import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
//
//import scala.concurrent.Future
//
//class TaxCalcServiceSpec extends UnitSpec
//  with MockSelfAssessmentConnector {
//
//  class Setup {
//    val service = new TaxCalcService()(
////      connector = mockSelfAssessmentConnector
//    )
//  }
//
//  implicit val request = FakeRequest()
//
//  "get" should {
//    "return a HttpResponse" when {
//      "the request contains a version 1.0 header and the connector preforms a successful get" in new Setup {
//        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+json"))
//        val response = HttpResponse(200)
//
//        MockSelfAssessmentConnector.get(request.uri)
//          .returns(Future.successful(Right(response)))
//
//        val result = await(service.get())
//        result shouldBe Right(response)
//      }
//    }
//
//    "return an UnsupportedAPIVersion error" when {
//      "the Accept header contains an unsupported API version" in new Setup {
//        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.5.0+json"))
//
//        val result = await(service.get())
//        result shouldBe Left(UnsupportedAPIVersion)
//      }
//    }
//
//    "return an IncorrectAPIVersion" when {
//      "the Accept header contains an incorrect value" in new Setup {
//        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "incorrect value"))
//
//        val result = await(service.get())
//        result shouldBe Left(IncorrectAPIVersion)
//      }
//    }
//  }
//
//  "post" should {
//
//    val requestBody = Json.obj("test" -> "body")
//
//    "return a HttpResponse" when {
//      "the request contains a version 1.0 header and the connector preforms a successful post" in new Setup {
//        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+json"))
//        val response = HttpResponse(200)
//
//        MockSelfAssessmentConnector.post(request.uri, requestBody)
//          .returns(Future.successful(Right(response)))
//
//        val result = await(service.post(requestBody))
//        result shouldBe Right(response)
//      }
//    }
//
//    "return an UnsupportedAPIVersion error" when {
//      "the Accept header contains an unsupported API version" in new Setup {
//        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.5.0+json"))
//
//        val result = await(service.post(requestBody))
//        result shouldBe Left(UnsupportedAPIVersion)
//      }
//    }
//
//    "return an IncorrectAPIVersion" when {
//      "the Accept header contains an incorrect value" in new Setup {
//        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "incorrect value"))
//
//        val result = await(service.post(requestBody))
//        result shouldBe Left(IncorrectAPIVersion)
//      }
//    }
//  }
//
//  "put" should {
//
//    val requestBody = Json.obj("test" -> "body")
//
//    "return a HttpResponse" when {
//      "the request contains a version 1.0 header and the connector preforms a successful post" in new Setup {
//        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+json"))
//        val response = HttpResponse(200)
//
//        MockSelfAssessmentConnector.put(request.uri, requestBody)
//          .returns(Future.successful(Right(response)))
//
//        val result = await(service.put(requestBody))
//        result shouldBe Right(response)
//      }
//    }
//
//    "return an UnsupportedAPIVersion error" when {
//      "the Accept header contains an unsupported API version" in new Setup {
//        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.5.0+json"))
//
//        val result = await(service.put(requestBody))
//        result shouldBe Left(UnsupportedAPIVersion)
//      }
//    }
//
//    "return an IncorrectAPIVersion" when {
//      "the Accept header contains an incorrect value" in new Setup {
//        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "incorrect value"))
//
//        val result = await(service.put(requestBody))
//        result shouldBe Left(IncorrectAPIVersion)
//      }
//    }
//  }
//
//  "apiVersion" should {
//
//    Seq(
//      "application/vnd.hmrc.1.0+json" -> "1.0",
//      "application/vnd.hmrc.0.0+json" -> "0.0",
//      "application/vnd.hmrc.9.9+json" -> "9.9"
//    ).foreach { case (headerValue, version) =>
//
//      "extract and return the version number from the Accept header" when {
//        s"the Accept header contains a value of $headerValue" in new Setup {
//          val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> headerValue))
//          service.getAPIVersionFromRequest(hc) shouldBe Some(version)
//        }
//      }
//    }
//
//    Seq(
//      "test incorrect value",
//      "application/vnd.hmrc.a.1+json",
//      "application/vnd.hmrc.1+json",
//      "application/vnd.hmrc.10+json",
//      "application/vnd.hmrc.-1.0+json",
//      "application/vnd.hmrc.10.0+json"
//    ).foreach { headerValue =>
//
//      "return a None" when {
//        s"the Accept header contains an incorrect value of $headerValue" in new Setup {
//          val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> headerValue))
//          service.getAPIVersionFromRequest(hc) shouldBe None
//        }
//      }
//    }
//  }
//}
