/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.audit

import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class AuditPayloadSpec extends JsonSpec {

  "PeriodicUpdateAuditPayload" should {
    "round trip" in {
      roundTripJson(PeriodicUpdateAuditPayload(Nino("AA999999A"), "abc", "def", Some("ghi"), Json.obj()))
    }
  }

  "TaxCalculationAuditPayload" should {
    "round trip" in {
      roundTripJson(TaxCalculationAuditPayload(Nino("AA999999A"), Some(TaxYear("2017-18")), "abc", Some(Json.obj())))
    }
  }
}
