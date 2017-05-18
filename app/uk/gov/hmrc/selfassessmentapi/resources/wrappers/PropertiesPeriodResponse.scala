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

import play.api.libs.json.Reads
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, DesErrorCode}
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties.{FHL, Other}
import uk.gov.hmrc.selfassessmentapi.models.{Period, PeriodId, PeriodSummary, des}

case class PropertiesPeriodResponse(underlying: HttpResponse) extends Response {
  def createLocationHeader(nino: Nino, id: PropertyType, periodId: PeriodId): String =
    s"/self-assessment/ni/$nino/uk-properties/$id/periods/$periodId"

  def transactionReference: Option[String] =
    (json \ "transactionReference").asOpt[String] match {
      case x @ Some(_) => x
      case None =>
        logger.error("The 'transactionReference' field was not found in the response from DES")
        None
    }

  def isInvalidPeriod: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_PERIOD)

  def isInvalidPayload: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_PAYLOAD)

  def isInvalidNino: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_NINO)
}

trait PeriodMapper[P <: Period, D <: des.properties.Period] {
  def from(d: D): P
  def setId(p: P, id: Option[String]): P
  def asSummary(p: P): PeriodSummary
}

object PeriodMapper {
  def apply[P <: Period, D <: des.properties.Period](implicit pm: PeriodMapper[P, D]): PeriodMapper[P, D] =
    implicitly

  implicit object OtherPeriodMapper extends PeriodMapper[Other.Properties, des.properties.Other.Properties] {
    override def from(d: des.properties.Other.Properties): Other.Properties =
      Other.Properties.from(d)

    override def setId(p: Other.Properties, id: Option[String]): Other.Properties =
      p.copy(id = id)

    override def asSummary(p: Other.Properties): PeriodSummary =
      p.asSummary
  }

  implicit object FHLPeriodMapper extends PeriodMapper[FHL.Properties, des.properties.FHL.Properties] {
    override def from(d: des.properties.FHL.Properties): FHL.Properties =
      FHL.Properties.from(d)

    override def setId(p: FHL.Properties, id: Option[String]): FHL.Properties =
      p.copy(id = id)

    override def asSummary(p: FHL.Properties): PeriodSummary =
      p.asSummary
  }
}

trait ResponseMapper[P <: Period, D <: des.properties.Period] {
  def period(response: PropertiesPeriodResponse)(implicit reads: Reads[D], pm: PeriodMapper[P, D]): Option[P] =
    response.underlying.json.asOpt[D] match {
      case Some(desPeriod) =>
        val from = PeriodMapper[P, D].from _
        val elideId = (p: P) => PeriodMapper[P, D].setId(p, None)
        Some((from andThen elideId)(desPeriod))
      case None =>
        response.logger.error("The response from DES does not match the expected properties period format.")
        None
    }

  def allPeriods(response: PropertiesPeriodResponse)(implicit reads: Reads[D],
                                                     pm: PeriodMapper[P, D]): Seq[PeriodSummary] =
    response.underlying.json.asOpt[Seq[D]] match {
      case Some(desPeriods) =>
        val asSummary = PeriodMapper[P, D].asSummary _
        val setId = (p: P) => PeriodMapper[P, D].setId(p, Some(p.periodId))
        val from = PeriodMapper[P, D].from _
        val fromDES = from andThen setId andThen asSummary
        desPeriods.map(fromDES(_)).sorted
      case None =>
        response.logger.error("The response from DES does not match the expected properties period format.")
        Seq.empty
    }
}

object ResponseMapper {
  def apply[P <: Period, D <: des.properties.Period](implicit rm: ResponseMapper[P, D]): ResponseMapper[P, D] =
    implicitly

  implicit object OtherResponseMapper extends ResponseMapper[Other.Properties, des.properties.Other.Properties]
  implicit object FHLResponseMapper extends ResponseMapper[FHL.Properties, des.properties.FHL.Properties]
}
