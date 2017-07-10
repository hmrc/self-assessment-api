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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, DesErrorCode, PeriodSummary => DesPeriodSummary}
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentPeriod
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.Response.periodsExceeding

case class SelfEmploymentPeriodResponse(underlying: HttpResponse) extends Response {
  def createLocationHeader(nino: Nino, id: SourceId, periodId: PeriodId): String = {
    s"/self-assessment/ni/$nino/${SourceType.SelfEmployments.toString}/$id/periods/$periodId"
  }

  def period: Option[SelfEmploymentPeriod] = {
    json.asOpt[des.selfemployment.SelfEmploymentPeriod] match {
      case Some(desPeriod) =>
        Some(SelfEmploymentPeriod.from(desPeriod).copy(id = None))
      case None =>
        logger.error("The response from DES does not match the expected self-employment period format.")
        None
    }
  }

  def allPeriods(maxPeriodTimeSpan: Int): Option[Seq[PeriodSummary]] = {
    (json \ "periods").asOpt[Seq[DesPeriodSummary]] match {
      case Some(desPeriods) =>
        val periods = desPeriods.map { period =>
          PeriodSummary(Period.id(period.from, period.to), period.from, period.to)
        }
        Some(periods.filter(periodsExceeding(maxPeriodTimeSpan)).sorted)
      case None =>
        logger.error("The response from DES does not match the expected self-employment period format.")
        None
    }
  }

  def transactionReference: Option[String] = {
    (json \ "transactionReference").asOpt[String] match {
      case x@Some(_) => x
      case None =>
        logger.error("The 'transactionReference' field was not found in the response from DES")
        None
    }
  }

  def isInvalidNino: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_NINO)

  def isInvalidPayload: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_PAYLOAD)

  def isInvalidPeriod: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_PERIOD)

  def isInvalidBusinessId: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_BUSINESSID)

  def isInvalidDateFrom: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_DATE_FROM)

  def isInvalidDateTo: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_DATE_TO)

}

