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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.http.Status._
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesBISSResourceFuncSpec extends BaseFunctionalSpec {

    "getSummary for Properties BISS" should {
      "return code 200 for a supplied valid data" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().PropertiesBISS.getSummary(nino, taxYear)
          .when()
          .get(s"/ni/$nino/uk-properties/$taxYear/income-summary")
          .thenAssertThat()
          .statusIs(200)
      }

      "return code 400 for a supplied invalid nino" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().PropertiesBISS.getSummaryErrorResponse(nino, taxYear, BAD_REQUEST, DesJsons.Errors.invalidIdValue)
          .when()
          .get(s"/ni/$nino/uk-properties/$taxYear/income-summary")
          .thenAssertThat()
          .statusIs(400)
      }

      "return multiple errors for a supplied invalid request" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().PropertiesBISS.getSummaryErrorResponse(nino, taxYear, BAD_REQUEST, DesJsons.Errors.multipleErrors)
          .when()
          .get(s"/ni/$nino/uk-properties/$taxYear/income-summary")
          .thenAssertThat()
          .statusIs(400)
      }

    }
}
