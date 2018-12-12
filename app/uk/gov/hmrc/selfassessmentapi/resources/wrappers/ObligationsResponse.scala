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
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.des.ObligationDetail

case class ObligationsResponse(underlying: HttpResponse) extends Response {

  def obligations(incomeSourceType: String, refNo: Option[SourceId] = None): Either[DesTransformError, Option[Obligations]] = {

    var errorMessage = s"The response from DES does not match the expected format. JSON: [$json]"

    def noneFound: Either[DesTransformError, Option[Obligations]] = {
      logger.warn(s"[ObligationsResponse] [obligations#noneFound] Error Message: $errorMessage")
      Right(None)
    }

    def oneFound(obligation: des.Obligations): Either[DesTransformError, Option[Obligations]] = {
      errorMessage = "Obligation for source id and/or business type was not found."
      val result = for {
        obl <- obligation.obligations
        check =
        if (refNo.isDefined)
          obl.identification.exists(identification => identification.referenceNumber == refNo.get) && obl.identification.get.incomeSourceType.contains(incomeSourceType)
        else
          obl.identification.exists(identification => identification.incomeSourceType.contains(incomeSourceType))
        if check
        detail <- obl.obligationDetails.filterNot(_.periodKey.contains("EOPS"))
      } yield DesTransformValidator[ObligationDetail, Obligation].from(detail)

      result.find(_.isLeft) match {
        case Some(ex) => Left(ex.left.get)
        case None => {
          val obligations: Seq[Obligation] = result map (_.right.get)
          if (obligations.isEmpty) Right(None) else Right(Some(Obligations(result map (_.right.get))))
        }
      }
    }

    json.asOpt[des.Obligations].fold(noneFound)(oneFound)
  }
}
