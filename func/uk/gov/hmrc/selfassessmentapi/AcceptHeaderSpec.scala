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

package uk.gov.hmrc.selfassessmentapi

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.support.BaseFunctionalSpec

class AcceptHeaderSpec extends BaseFunctionalSpec {
  val selfEmploymentId: String = BSONObjectID.generate.stringify

  "if the valid content type header is sent in the request, they" should {
    "receive 200" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.noneFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments").withAcceptHeader()
        .thenAssertThat().statusIs(200).jsonBodyIsEmptyArray()
    }
  }

  "if the valid content type header is missing in the request, they" should {
    "receive 406" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(406)
        .bodyIsError("ACCEPT_HEADER_INVALID")
    }
  }

}
