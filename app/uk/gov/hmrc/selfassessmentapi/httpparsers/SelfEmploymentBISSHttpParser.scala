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

package uk.gov.hmrc.selfassessmentapi.httpparsers

import play.api.Logger
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.models.Errors._
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentBISS

object SelfEmploymentBISSHttpParser {
  type SelfEmploymentBISSOutcome = Either[Error, SelfEmploymentBISS]
}

trait SelfEmploymentBISSHttpParser extends HttpParser {
  import SelfEmploymentBISSHttpParser._

  implicit val selfEmploymentBISSHttpParser = new HttpReads[SelfEmploymentBISSOutcome] {
    override def read(method: String, url: String, response: HttpResponse): SelfEmploymentBISSOutcome = {
      (response.status, response.jsonOpt) match {
        case (OK, _) => response.jsonOpt.validate[SelfEmploymentBISS].fold(
          invalid => {
            Logger.warn(s"[selfEmploymentBISSHttpParser] - Error reading DES Response: $invalid")
            Left(ServerError)
          },
          valid => Right(valid)
        )
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_NINO)) => {
          Logger.warn(s"[selfEmploymentBISSHttpParser] - Invalid Nino")
          Left(NinoInvalid)
        }
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_TAX_YEAR)) => {
          Logger.warn(s"[selfEmploymentBISSHttpParser] - Invalid tax year")
          Left(TaxYearInvalid)
        }
        case (NOT_FOUND, ErrorCode(DesErrorCode.NOT_FOUND_NINO)) => {
          Logger.warn(s"[selfEmploymentBISSHttpParser] - Nino not found")
          Left(NinoNotFound)
        }
        case (NOT_FOUND, ErrorCode(DesErrorCode.NOT_FOUND_TAX_YEAR)) => {
          Logger.warn(s"[selfEmploymentBISSHttpParser] - Tax year not found")
          Left(TaxYearNotFound)
        }
        case (NOT_FOUND, ErrorCode(DesErrorCode.NOT_FOUND)) => {
          Logger.warn(s"[selfEmploymentBISSHttpParser] - No submissions data exists for provided tax year")
          Left(NoSubmissionDataExists)
        }
        case (NOT_FOUND, ErrorCode(DesErrorCode.NOT_FOUND_INCOME_SOURCE)) => {
          Logger.warn(s"[selfEmploymentBISSHttpParser] - No submissions data can be found for the self-employment ID")
          Left(SelfEmploymentIDNotFound)
        }
        case (INTERNAL_SERVER_ERROR, ErrorCode(DesErrorCode.SERVER_ERROR)) => {
          Logger.warn(s"[selfEmploymentBISSHttpParser] - An error has occurred with DES")
          Left(ServerError)
        }
        case (SERVICE_UNAVAILABLE, ErrorCode(DesErrorCode.SERVICE_UNAVAILABLE)) => {
          Logger.warn(s"[selfEmploymentBISSHttpParser] - DES is currently down")
          Left(ServiceUnavailable)
        }
        case (status, _) =>
          Logger.warn(s"[selfEmploymentBISSHttpParser] - Non-OK DES Response: STATUS $status ------- \n ${response.body}")
          Left(ServerError)
      }
    }
  }
}
