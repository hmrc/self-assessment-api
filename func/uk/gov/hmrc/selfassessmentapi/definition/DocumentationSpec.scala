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

package uk.gov.hmrc.selfassessmentapi.definition

import uk.gov.hmrc.support.BaseFunctionalSpec

class DocumentationSpec extends BaseFunctionalSpec {


  "Request to /api/definition" should {
    "return 404" in {
      given()
        .when()
        .get("/api/definition")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "Request to /api/conf/1.0/application.raml" should {
    "return 404" in {
      given()
        .when()
        .get("/api/conf/1.0/application.raml")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
