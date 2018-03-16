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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import org.joda.time.LocalDate
import org.scalatest.EitherValues
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, AccountingType}
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentRetrieve

class SelfEmploymentResponseSpec extends UnitSpec with EitherValues {
  private val nino = generateNino

  "createLocation" should {
    "return a string containing the location with an ID extracted from some JSON" in {
      val json =
        Json.parse("""
                     |{
                     | "incomeSources": [
                     |   {
                     |     "incomeSourceId": "abc"
                     |   }
                     | ]
                     |}
                   """.stripMargin)

      val wrapper = SelfEmploymentResponse(HttpResponse(200, responseJson = Some(json)))

      wrapper.createLocationHeader(nino) shouldBe Some(s"/self-assessment/ni/$nino/self-employments/abc")
    }

    "return None for a json that does not contain an income source ID" in {
      val wrapper = SelfEmploymentResponse(HttpResponse(200, responseJson = None))

      wrapper.createLocationHeader(nino) shouldBe None
    }
  }

  "selfEmployment" should {
    "return EmptyBusinessData if business data is not present in the json response" in {
      val json = Json.parse("{}")
      val response = SelfEmploymentResponse(HttpResponse(200, Some(json)))
      response.selfEmployment("someId").left.value shouldBe an[EmptyBusinessData]
    }

    "return EmptySelfEmployments if the list of self-employments is empty" in {
      val json = Json.parse("""{ "businessData": [] }""")
      val response = SelfEmploymentResponse(HttpResponse(200, Some(json)))
      response.selfEmployment("someId").left.value shouldBe an[EmptySelfEmployments]
    }

    "return UnmatchedIncomeId if the supplied self-employment id does not match or is not found in the response" in {
      val json = Json.parse("""
                              |{
                              |   "safeId": "XE00001234567890",
                              |   "nino": "AA123456A",
                              |   "mtdbsa": "123456789012345",
                              |   "propertyIncome": false,
                              |   "businessData": [
                              |      {
                              |         "incomeSourceId": "123456789012345",
                              |         "accountingPeriodStartDate": "2001-01-01",
                              |         "accountingPeriodEndDate": "2001-01-01",
                              |         "tradingName": "RCDTS",
                              |         "businessAddressDetails": {
                              |            "addressLine1": "100 SuttonStreet",
                              |            "addressLine2": "Wokingham",
                              |            "addressLine3": "Surrey",
                              |            "addressLine4": "London",
                              |            "postalCode": "DH14EJ",
                              |            "countryCode": "GB"
                              |         },
                              |         "businessContactDetails": {
                              |            "phoneNumber": "01332752856",
                              |            "mobileNumber": "07782565326",
                              |            "faxNumber": "01332754256",
                              |            "emailAddress": "stephen@manncorpone.co.uk"
                              |         },
                              |         "tradingStartDate": "2001-01-01",
                              |         "cashOrAccruals": "cash",
                              |         "seasonal": true
                              |      }
                              |   ]
                              |}
                            """.stripMargin)
      val response = SelfEmploymentResponse(HttpResponse(200, Some(json)))
      response.selfEmployment("someId").left.value shouldBe an[UnmatchedIncomeId]
    }

    "return UnableToMapAccountingType if the accounting type cannot be found in the response" in {
      val json = Json.parse("""
                              |{
                              |   "safeId": "XE00001234567890",
                              |   "nino": "AA123456A",
                              |   "mtdbsa": "123456789012345",
                              |   "propertyIncome": false,
                              |   "businessData": [
                              |      {
                              |         "incomeSourceId": "123456789012345",
                              |         "accountingPeriodStartDate": "2001-01-01",
                              |         "accountingPeriodEndDate": "2001-01-01",
                              |         "tradingName": "RCDTS",
                              |         "businessAddressDetails": {
                              |            "addressLine1": "100 SuttonStreet",
                              |            "addressLine2": "Wokingham",
                              |            "addressLine3": "Surrey",
                              |            "addressLine4": "London",
                              |            "postalCode": "DH14EJ",
                              |            "countryCode": "GB"
                              |         },
                              |         "businessContactDetails": {
                              |            "phoneNumber": "01332752856",
                              |            "mobileNumber": "07782565326",
                              |            "faxNumber": "01332754256",
                              |            "emailAddress": "stephen@manncorpone.co.uk"
                              |         },
                              |         "tradingStartDate": "2001-01-01",
                              |         "cashOrAccruals": "INVALID ACCOUNTING TYPE",
                              |         "seasonal": true
                              |      }
                              |   ]
                              |}
                            """.stripMargin)
      val response = SelfEmploymentResponse(HttpResponse(200, Some(json)))
      response.selfEmployment("123456789012345").left.value shouldBe an[UnableToMapAccountingType]
    }

    "return Parse error if the json cannot be parsed" in {
      val json = Json.parse("""{ "businessData": 1 }""")
      val response = SelfEmploymentResponse(HttpResponse(200, Some(json)))
      response.selfEmployment("someId").left.value shouldBe an[ParseError]
    }

    "return valid self-employment if the response is valid" in {
      val json = Json.parse("""
                              |{
                              |   "safeId": "XE00001234567890",
                              |   "nino": "AA123456A",
                              |   "mtdbsa": "123456789012345",
                              |   "propertyIncome": false,
                              |   "businessData": [
                              |      {
                              |         "incomeSourceId": "123456789012345",
                              |         "accountingPeriodStartDate": "2001-01-01",
                              |         "accountingPeriodEndDate": "2001-01-01",
                              |         "tradingName": "RCDTS",
                              |         "businessAddressDetails": {
                              |            "addressLine1": "100 SuttonStreet",
                              |            "addressLine2": "Wokingham",
                              |            "addressLine3": "Surrey",
                              |            "addressLine4": "London",
                              |            "postalCode": "DH14EJ",
                              |            "countryCode": "GB"
                              |         },
                              |         "businessContactDetails": {
                              |            "phoneNumber": "01332752856",
                              |            "mobileNumber": "07782565326",
                              |            "faxNumber": "01332754256",
                              |            "emailAddress": "stephen@manncorpone.co.uk"
                              |         },
                              |         "tradingStartDate": "2001-01-01",
                              |         "cashOrAccruals": "cash",
                              |         "seasonal": true
                              |      }
                              |   ]
                              |}
                            """.stripMargin)
      val response = SelfEmploymentResponse(HttpResponse(200, Some(json)))
      response.selfEmployment("123456789012345").right.value shouldBe SelfEmploymentRetrieve(
        None,
        AccountingPeriod(LocalDate.parse("2001-01-01"), LocalDate.parse("2001-01-01")),
        AccountingType.CASH,
        Some(LocalDate.parse("2001-01-01")),
        None,
        "RCDTS",
        None,
        Some("100 SuttonStreet"),
        Some("Wokingham"),
        Some("Surrey"),
        Some("London"),
        Some("DH14EJ"))
    }
  }

  "listSelfEmployment" should {
    "return EmptyBusinessData if business data is not present in the json response" in {
      val json = Json.parse("{}")
      val response = SelfEmploymentResponse(HttpResponse(200, Some(json)))
      response.listSelfEmployment.left.value shouldBe an[EmptyBusinessData]
    }

    "return EmptySelfEmployments if the list of self-employments is empty" in {
      val json = Json.parse("""{ "businessData": [] }""")
      val response = SelfEmploymentResponse(HttpResponse(200, Some(json)))
      response.listSelfEmployment.left.value shouldBe an[EmptySelfEmployments]
    }

    "return UnableToMapAccountingType if there is at least one self-employment with an invalid accounting type in the response" in {
      val json = Json.parse("""
                              |{
                              |   "safeId": "XE00001234567890",
                              |   "nino": "AA123456A",
                              |   "mtdbsa": "123456789012345",
                              |   "propertyIncome": false,
                              |   "businessData": [
                              |      {
                              |         "incomeSourceId": "123456789012345",
                              |         "accountingPeriodStartDate": "2001-01-01",
                              |         "accountingPeriodEndDate": "2001-01-01",
                              |         "tradingName": "RCDTS",
                              |         "businessAddressDetails": {
                              |            "addressLine1": "100 SuttonStreet",
                              |            "addressLine2": "Wokingham",
                              |            "addressLine3": "Surrey",
                              |            "addressLine4": "London",
                              |            "postalCode": "DH14EJ",
                              |            "countryCode": "GB"
                              |         },
                              |         "businessContactDetails": {
                              |            "phoneNumber": "01332752856",
                              |            "mobileNumber": "07782565326",
                              |            "faxNumber": "01332754256",
                              |            "emailAddress": "stephen@manncorpone.co.uk"
                              |         },
                              |         "tradingStartDate": "2001-01-01",
                              |         "cashOrAccruals": "INVALID ACCOUNTING TYPE",
                              |         "seasonal": true
                              |      },
                              |      {
                              |         "incomeSourceId": "123456789012346",
                              |         "accountingPeriodStartDate": "2001-01-01",
                              |         "accountingPeriodEndDate": "2001-01-01",
                              |         "tradingName": "RCDTS",
                              |         "businessAddressDetails": {
                              |            "addressLine1": "100 SuttonStreet",
                              |            "addressLine2": "Wokingham",
                              |            "addressLine3": "Surrey",
                              |            "addressLine4": "London",
                              |            "postalCode": "DH14EJ",
                              |            "countryCode": "GB"
                              |         },
                              |         "businessContactDetails": {
                              |            "phoneNumber": "01332752856",
                              |            "mobileNumber": "07782565326",
                              |            "faxNumber": "01332754256",
                              |            "emailAddress": "stephen@manncorpone.co.uk"
                              |         },
                              |         "tradingStartDate": "2001-01-01",
                              |         "cashOrAccruals": "cash",
                              |         "seasonal": true
                              |      }
                              |   ]
                              |}
                            """.stripMargin)
      val response = SelfEmploymentResponse(HttpResponse(200, Some(json)))
      response.listSelfEmployment.left.value shouldBe an[UnableToMapAccountingType]
    }

    "return Parse error if the json cannot be parsed" in {
      val json = Json.parse("""{ "businessData": 1 }""")
      val response = SelfEmploymentResponse(HttpResponse(200, Some(json)))
      response.listSelfEmployment.left.value shouldBe an[ParseError]
    }

    "return a list of valid self-employments if the response is valid" in {
      val json = Json.parse("""
                              |{
                              |   "safeId": "XE00001234567890",
                              |   "nino": "AA123456A",
                              |   "mtdbsa": "123456789012345",
                              |   "propertyIncome": false,
                              |   "businessData": [
                              |      {
                              |         "incomeSourceId": "123456789012345",
                              |         "accountingPeriodStartDate": "2001-01-01",
                              |         "accountingPeriodEndDate": "2001-01-01",
                              |         "tradingName": "RCDTS",
                              |         "businessAddressDetails": {
                              |            "addressLine1": "100 SuttonStreet",
                              |            "addressLine2": "Wokingham",
                              |            "addressLine3": "Surrey",
                              |            "addressLine4": "London",
                              |            "postalCode": "DH14EJ",
                              |            "countryCode": "GB"
                              |         },
                              |         "businessContactDetails": {
                              |            "phoneNumber": "01332752856",
                              |            "mobileNumber": "07782565326",
                              |            "faxNumber": "01332754256",
                              |            "emailAddress": "stephen@manncorpone.co.uk"
                              |         },
                              |         "tradingStartDate": "2001-01-01",
                              |         "cashOrAccruals": "accruals",
                              |         "seasonal": true
                              |      },
                              |      {
                              |         "incomeSourceId": "123456789012346",
                              |         "accountingPeriodStartDate": "2001-01-01",
                              |         "accountingPeriodEndDate": "2001-01-01",
                              |         "tradingName": "RCDTS",
                              |         "businessAddressDetails": {
                              |            "addressLine1": "100 SuttonStreet",
                              |            "addressLine2": "Wokingham",
                              |            "addressLine3": "Surrey",
                              |            "addressLine4": "London",
                              |            "postalCode": "DH14EJ",
                              |            "countryCode": "GB"
                              |         },
                              |         "businessContactDetails": {
                              |            "phoneNumber": "01332752856",
                              |            "mobileNumber": "07782565326",
                              |            "faxNumber": "01332754256",
                              |            "emailAddress": "stephen@manncorpone.co.uk"
                              |         },
                              |         "tradingStartDate": "2001-01-01",
                              |         "cashOrAccruals": "cash",
                              |         "seasonal": true
                              |      }
                              |   ]
                              |}
                            """.stripMargin)
      val response = SelfEmploymentResponse(HttpResponse(200, Some(json)))
      response.listSelfEmployment.right.value should contain theSameElementsAs Seq(
        SelfEmploymentRetrieve(Some("123456789012345"),
          AccountingPeriod(LocalDate.parse("2001-01-01"), LocalDate.parse("2001-01-01")),
          AccountingType.ACCRUAL,
          Some(LocalDate.parse("2001-01-01")),
          None,
          "RCDTS",
          None,
          Some("100 SuttonStreet"),
          Some("Wokingham"),
          Some("Surrey"),
          Some("London"),
          Some("DH14EJ")),
        SelfEmploymentRetrieve(Some("123456789012346"),
          AccountingPeriod(LocalDate.parse("2001-01-01"), LocalDate.parse("2001-01-01")),
          AccountingType.CASH,
          Some(LocalDate.parse("2001-01-01")),
          None,
          "RCDTS",
          None,
          Some("100 SuttonStreet"),
          Some("Wokingham"),
          Some("Surrey"),
          Some("London"),
          Some("DH14EJ")))
    }
  }
}
