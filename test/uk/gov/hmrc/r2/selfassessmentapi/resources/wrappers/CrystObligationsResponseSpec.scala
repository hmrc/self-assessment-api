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

package uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.models.{CrystObligation, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.Jsons.Obligations.crystallisationObligations

class CrystObligationsResponseSpec extends UnitSpec {

  "CrystObligationResponse" should {
    "valid response" in {
      val taxYear = TaxYear("2017-18")
      val response = CrystObligationsResponse(HttpResponse(200, Some(Json.parse(crystallisationObligations("PL923388A", taxYear)))))

      response.obligations("ITSA",Nino("PL923388A"), taxYear.taxYearFromDate) match {
        case Right(obligation) => obligation match {
            case Some(obl) => obl shouldEqual CrystObligation(start = new LocalDate("2017-04-06"),
                                                         end = new LocalDate("2018-04-05"),
                                                         due = Some(new LocalDate("2019-01-31")),
                                                         met = false)
            case None => fail("Expected obligation was not defined!")
          }
        case Left(exc) => fail(s"Transformation error occurred, details: $exc")
      }
    }
  }
}
