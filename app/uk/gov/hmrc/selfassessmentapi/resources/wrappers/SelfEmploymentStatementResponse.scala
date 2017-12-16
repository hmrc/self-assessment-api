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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import cats.instances.{map, tuple}
import org.joda.time.LocalDate
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, DesErrorCode, ObligationDetail}
import uk.gov.hmrc.selfassessmentapi.models.{DesTransformError, DesTransformValidator, EopsObligation, EopsObligations, SourceId, des}
import uk.gov.hmrc.selfassessmentapi.resources.utils.ObligationQueryParams

case class SelfEmploymentStatementResponse(underlying: HttpResponse) extends Response {

  def statement(incomeSourceType: String, id: SourceId, params: ObligationQueryParams): Either[DesTransformError, Option[EopsObligations]] = {

    val desObligations = json.asOpt[des.Obligations]

    var errorMessage = s"The response from DES does not match the expected format. JSON: [$json]"

    def noneFound: Either[DesTransformError, Option[EopsObligations]] = {
      logger.error(errorMessage)
      Right(None)
    }

    def oneFound(obligation: des.Obligations): Either[DesTransformError, Option[EopsObligations]] = {
      errorMessage = "Obligation for source id and/or business type was not found."

      obligation.obligations.find(o => o.id.fold(false)(_ == id) &&
                                  o.`type` == incomeSourceType).fold(noneFound) {
        desObligation =>
          val obligationsOrError: Seq[Either[DesTransformError, EopsObligation]] = for {
            details <- desObligation.details
            if params.from.fold(true)(new LocalDate(details.inboundCorrespondenceFromDate).isAfter) &&
              params.to.fold(true)(new LocalDate(details.inboundCorrespondenceToDate).isBefore) &&
              params.status.fold(true)(details.status == _)
          } yield DesTransformValidator[ObligationDetail, EopsObligation].from(details)

          obligationsOrError.find(_.isLeft) match {
            case Some(ex) => Left(ex.left.get)
            case None => Right(Some(EopsObligations(obligations = obligationsOrError map (_.right.get))))
          }
      }

    }

    desObligations.fold(noneFound)(oneFound)
  }


  def statement(incomeSourceType: String, params: ObligationQueryParams): Either[DesTransformError, Option[Seq[EopsObligations]]] = {

    val desObligations = json.asOpt[des.Obligations]

    var errorMessage = s"The response from DES does not match the expected format. JSON: [$json]"

    def noneFound: Either[DesTransformError, Option[Seq[EopsObligations]]] = {
      logger.error(errorMessage)
      Right(None)
    }

    def oneFound(obligation: des.Obligations): Either[DesTransformError, Option[Seq[EopsObligations]]] = {
      errorMessage = "Obligation for source id and/or business type was not found."


      val sourceIdToErrorOrEopsObligation = for {
        o <- obligation.obligations
        if o.`type` == incomeSourceType
        details <- o.details
        if params.from.fold(true)(new LocalDate(details.inboundCorrespondenceFromDate).isAfter) &&
          params.to.fold(true)(new LocalDate(details.inboundCorrespondenceToDate).isBefore) &&
          params.status.fold(true)(details.status == _)
      } yield o.id -> DesTransformValidator[ObligationDetail, EopsObligation].from(details)

      type EDE  = Either[DesTransformError, EopsObligation]

      def reorder(map: Map[Option[SourceId], Seq[EDE]], tuple: (Option[SourceId], EDE)) = {
        if (map.keySet.contains(tuple._1))
          map.updated(tuple._1, map(tuple._1) :+ tuple._2)
        else
          map + (tuple._1 -> Seq(tuple._2))
      }

      sourceIdToErrorOrEopsObligation.find(candid => candid._2.isLeft) match {
        case Some(ex) => Left(ex._2.left.get)
        case None =>
          Right(
            Some(
              (for {
                (id, obligation) <- sourceIdToErrorOrEopsObligation.foldLeft(Map[Option[SourceId], Seq[EDE]]())(reorder)
              } yield EopsObligations(id, obligation.map (_.right.get))).toSeq
            )
          )
      }
    }

    desObligations.fold(noneFound)(oneFound)
  }

  def isInvalidNino: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_NINO)
}