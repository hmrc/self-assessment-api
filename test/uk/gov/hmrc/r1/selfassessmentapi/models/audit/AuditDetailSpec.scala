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

package uk.gov.hmrc.r1.selfassessmentapi.models.audit

import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

class AuditDetailSpec extends JsonSpec {

  "PeriodicUpdateAuditPayload" should {
    "round trip" in {
      roundTripJson(
        PeriodicUpdate(
          auditType = "amendPeriodicUpdate",
          httpStatus = 200,
          nino = Nino("AA999999A"),
          sourceId = "abc",
          periodId = "def",
          affinityGroup = "individual",
          agentCode = None,
          transactionReference = Some("ghi"),
          requestPayload = Json.obj(),
          responsePayload = Some(Json.obj())
        ))
    }
  }

  "TaxCalculationTrigger" should {
    "round trip" in {
      roundTripJson(
        TaxCalculationTrigger(
          httpStatus = 200,
          nino = Nino("AA999999A"),
          taxYear = TaxYear("2017-18"),
          affinityGroup = "individual",
          agentCode = None,
          calculationId = Some("abc"),
          responsePayload = Some(Json.obj())
        ))
    }
  }

  "TaxCalculationRequest" should {
    "round trip" in {
      roundTripJson(
        TaxCalculationRequest(httpStatus = 200,
                              nino = Nino("AA999999A"),
                              calculationId = "abc",
                              affinityGroup = "individual",
                              agentCode = None,
                              responsePayload = Some(Json.obj())))
    }
  }

  "RetrieveObligations" should {
    "round trip" in {
      roundTripJson(
        RetrieveObligations(httpStatus = 200,
                            nino = Nino("AA999999A"),
                            sourceId = Some("abc"),
                            affinityGroup = "individual",
                            agentCode = None,
                            responsePayload = Some(Json.obj())))
    }
  }
}
