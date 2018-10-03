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

import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models.des.ObligationDetail
import uk.gov.hmrc.selfassessmentapi.models.{DesTransformError, DesTransformValidator, EopsObligation, EopsObligations, SourceId, des}

case class SelfEmploymentStatementResponse(underlying: HttpResponse) extends Response {

  def retrieveEOPSObligation(identifier: SourceId): Either[DesTransformError, Option[EopsObligations]] = {

    val incomeSourceType = "ITSB"

    val desObligations = json.asOpt[des.Obligations]

    var errorMessage = s"The response from DES does not match the expected format. JSON: [$json]"

    def noneFound: Either[DesTransformError, Option[EopsObligations]] = {
      logger.warn(errorMessage)
      Right(None)
    }

    def oneFound(obligation: des.Obligations): Either[DesTransformError, Option[EopsObligations]] = {
      errorMessage = "Obligation for source id and/or business type was not found."
      val result = for {
        obl <- obligation.obligations
        if obl.identification.exists(
          identification => identification.referenceNumber == identifier && identification.incomeSourceType.contains(incomeSourceType)
        )
        details <- obl.obligationDetails.filter(_.periodKey.contains("EOPS"))
      } yield DesTransformValidator[ObligationDetail, EopsObligation].from(details)

      result.find(_.isLeft) match {
        case Some(ex) =>
          Left(ex.left.get)
        case None =>
          val obligations: Seq[EopsObligation] = result map (_.right.get)
          if (obligations.isEmpty) Right(None) else Right(Some(EopsObligations(obligations = obligations)))
      }
    }
    desObligations.fold(noneFound)(oneFound)
  }
}
