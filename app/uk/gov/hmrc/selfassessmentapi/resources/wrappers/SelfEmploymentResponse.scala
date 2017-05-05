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
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, DesErrorCode}
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentRetrieve
import uk.gov.hmrc.selfassessmentapi.models.{SourceId, des}

case class SelfEmploymentResponse(underlying: HttpResponse) extends Response {

  def createLocationHeader(nino: Nino): Option[String] =
    (json \ "incomeSources" \\ "incomeSourceId").map(_.asOpt[String]) match {
      case Some(id) +: _ => Some(s"/self-assessment/ni/$nino/self-employments/$id")
      case _ =>
        logger.error("The 'incomeSourceId' field was not found in the response from DES.")
        None
    }

  def selfEmployment(id: SourceId): Option[SelfEmploymentRetrieve] =
    (json \ "businessData").asOpt[Seq[des.SelfEmployment]] match {
      case Some(selfEmployments) =>
        for {
          desSe <- selfEmployments.find(_.incomeSourceId.exists(_ == id))
          se <- SelfEmploymentRetrieve.from(desSe)
        } yield se.copy(id = None)
      case None =>
        logger.error("The 'businessData' field was not found in the response from DES")
        None
    }

  def listSelfEmployment: Seq[SelfEmploymentRetrieve] =
    (json \ "businessData").asOpt[Seq[des.SelfEmployment]] match {
      case Some(selfEmployments) =>
        selfEmployments.flatMap(SelfEmploymentRetrieve.from)
      case None =>
        logger.error("The 'businessData' field was not found in the response from DES")
        Seq.empty
    }

  def isInvalidNino: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_NINO)

}
