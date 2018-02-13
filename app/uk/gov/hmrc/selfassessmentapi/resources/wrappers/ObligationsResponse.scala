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
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, DesErrorCode, ObligationDetail}

case class ObligationsResponse(underlying: HttpResponse) extends Response {

  def obligations(incomeSourceType: String, id: Option[SourceId] = None): Either[DesTransformError, Option[Obligations]] = {

    val desObligations = json.asOpt[des.Obligations]

    var errorMessage = s"The response from DES does not match the expected format. JSON: [$json]"

    def noneFound: Either[DesTransformError, Option[Obligations]] = {
      logger.error(errorMessage)
      Right(None)
    }

    def oneFound(obligation: des.Obligations): Either[DesTransformError, Option[Obligations]] = {
      errorMessage = "Obligation for source id and/or business type was not found."
      obligation.obligations.find(o => o.id.forall(oId => id.forall(_ == oId)) && o.`type` == incomeSourceType).fold(noneFound) {
        desObligation =>
          val obligationsOrError: Seq[Either[DesTransformError, Obligation]] = for {
            details <- desObligation.details
          } yield DesTransformValidator[ObligationDetail, Obligation].from(details)

          obligationsOrError.find(_.isLeft) match {
            case Some(ex) => Left(ex.left.get)
            case None => Right(Some(Obligations(obligationsOrError map (_.right.get))))

          }
      }
    }

    desObligations.fold(noneFound)(oneFound)
  }

  // used for the new format of the obligation (returned by the API 1330)
  def obligations(incomeSourceType: String, nino: Nino, taxYearFromDate: LocalDate): Either[DesTransformError, Option[Obligation]] = {

    val desObligations = json.asOpt[des.ObligationsNew]

    def noneFound: Either[DesTransformError, Option[Obligation]] = {
      logger.error(s"The response from DES does not match the expected format. JSON: [$json]")
      Right(None)
    }

    def oneFound(obligation: des.ObligationsNew): Either[DesTransformError, Option[Obligation]] = {
      (for {
        obl <- obligation.obligations
        id = obl.identification
        if id.incomeSourceType == incomeSourceType && id.referenceNumber == nino.nino
        details <- obl.obligationDetails
        if taxYearFromDate == new LocalDate(details.inboundCorrespondenceFromDate)
      } yield details).map(toObligation(_))
        .headOption.fold[Either[DesTransformError, Option[Obligation]]](Right(None)) {
        case Right(obl) => Right(Some(obl))
        case Left(error) => Left(error)
      }
    }

    desObligations.fold(noneFound)(oneFound)
  }

  def isInvalidNino: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_NINO)

}
