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
import org.joda.time.LocalDate.parse
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models._

case class CrystObligationsResponse(underlying: HttpResponse) extends Response {

  // used for the new format of the obligation (returned by the API 1330)
  def obligations(incomeSourceType: String, nino: Nino, taxYearFromDate: LocalDate): Either[DesTransformError, Option[CrystObligation]] = {

    def noneFound: Either[DesTransformError, Option[CrystObligation]] = {
      logger.warn(s"The response from DES does not match the expected format. JSON: [$json]")
      Right(None)
    }

    def oneFound(obligation: des.CrystObligations): Either[DesTransformError, Option[CrystObligation]] = {
      (for {
        obl <- obligation.obligations
        id = obl.identification
        if id.incomeSourceType.contains(incomeSourceType) && id.referenceNumber == nino.nino
        details <- obl.obligationDetails
        if taxYearFromDate == parse(details.inboundCorrespondenceFromDate)
      } yield details).map(toObligation(_))
        .headOption.fold[Either[DesTransformError, Option[CrystObligation]]](Right(None)) {
        case Right(obl) => Right(Some(obl))
        case Left(error) => Left(error)
      }
    }

    json.asOpt[des.CrystObligations].fold(noneFound)(oneFound)
  }
}
