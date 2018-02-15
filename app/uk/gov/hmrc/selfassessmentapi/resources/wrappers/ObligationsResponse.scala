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

  def obligations(incomeSourceType: String, id: Option[SourceId] = None): Either[DesTransformError, Option[Obligations]] = {

    val desObligations = json.asOpt[des.Obligations]

    var errorMessage = s"The response from DES does not match the expected format. JSON: [$json]"

    def noneFound: Either[DesTransformError, Option[Obligations]] = {
      logger.error(errorMessage)
      Right(None)
    }

    def oneFound(obligation: des.Obligations): Either[DesTransformError, Option[Obligations]] = {
      errorMessage = "Obligation for source id and/or business type was not found."
      val result = for {
        obl <- obligation.obligations
        if (id.isEmpty || obl.id == id) && obl.`type` == incomeSourceType
        detail <- obl.details
      } yield DesTransformValidator[ObligationDetail, Obligation].from(detail)

      result.find(_.isLeft) match {
        case Some(ex) => Left(ex.left.get)
        case None => Right(Some(Obligations(result map (_.right.get))))
      }
    }

    desObligations.fold(noneFound)(oneFound)
  }
}
