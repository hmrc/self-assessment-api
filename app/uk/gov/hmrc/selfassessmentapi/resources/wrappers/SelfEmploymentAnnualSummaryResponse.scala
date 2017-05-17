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

import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models.des
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentAnnualSummary

case class SelfEmploymentAnnualSummaryResponse(underlying: HttpResponse) extends Response {

  def annualSummary: Option[SelfEmploymentAnnualSummary] = {
    json.asOpt[des.SelfEmploymentAnnualSummary] match {
      case Some(desSummary) => Some(SelfEmploymentAnnualSummary.from(desSummary))
      case None => {
        logger.error("The response from DES does not match the expected self-employment annual summary format.")
        None
      }
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
}
