/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.services

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.domain.AnnualSummaryContainer
import uk.gov.hmrc.selfassessmentapi.resources.models.{AnnualSummary, SourceId, TaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AnnualSummaryService[A <: AnnualSummary, C <: AnnualSummaryContainer[A]] {

  val annualSummaryRepository: NewSourceRepository[String, C]

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear, a: A): Future[Boolean]

  def retrieveAnnualSummary(id: SourceId, taxYear: TaxYear, nino: Nino): Future[Option[A]]
}
