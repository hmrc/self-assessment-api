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

package uk.gov.hmrc.selfassessmentapi.services

import org.joda.time.LocalDate
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.models.{Obligation, Obligations}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait PropertiesObligationsService {
  val repository: PropertiesRepository

  def retrieveObligations(nino: Nino, header: Option[String]): Future[Option[Obligations]] = {
    repository.retrieve(nino) map {
      case Some(_) => Some(selectCannedResponse(header))
      case None => None
    }
  }

  private def selectCannedResponse(header: Option[String]): Obligations = {
    header match {
      case Some("FIRST_MET") =>
        cannedResponse(firstMet = true)
      case Some("ALL_MET") =>
        cannedResponse(firstMet = true, secondMet = true, thirdMet = true, fourthMet = true)
      case Some(_) | None =>
        cannedResponse()
    }
  }

  private def cannedResponse(firstMet: Boolean = false, secondMet: Boolean = false,
                             thirdMet: Boolean = false, fourthMet: Boolean = false) = {
    Obligations(
      Seq(
        Obligation(LocalDate.parse("2017-04-06"), LocalDate.parse("2017-07-05"), LocalDate.parse("2017-08-05"), firstMet),
        Obligation(LocalDate.parse("2017-07-06"), LocalDate.parse("2017-10-05"), LocalDate.parse("2017-11-05"), secondMet),
        Obligation(LocalDate.parse("2017-10-06"), LocalDate.parse("2018-01-05"), LocalDate.parse("2018-02-05"), thirdMet),
        Obligation(LocalDate.parse("2018-01-06"), LocalDate.parse("2018-04-05"), LocalDate.parse("2018-05-06"), fourthMet)
      ).sorted
    )
  }
}

object PropertiesObligationsService extends PropertiesObligationsService {
  override val repository: PropertiesRepository = PropertiesRepository()
}
