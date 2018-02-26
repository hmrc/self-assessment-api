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

import uk.gov.hmrc.support.BaseFunctionalSpec

class GiftAidPaymentsResourceFunctionalSpec  extends BaseFunctionalSpec {

  "update gift aids payments" should {
    "return code 204 when updating payments" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .des()
        .GiftAid
        .updatePayments(nino, taxYear)
        .when()
        .put(Jsons.GiftAidPayments(100))
        .at(s"/ni/$nino/gift-aid/$taxYear")
        .thenAssertThat()
        .statusIs(204)
    }
  }

}
