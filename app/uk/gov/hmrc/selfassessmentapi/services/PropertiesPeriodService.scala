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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.domain.PropertyPeriodOps
import uk.gov.hmrc.selfassessmentapi.domain.PropertyPeriodOps._
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.properties.{FHL, Other}
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait PropertiesPeriodService[P <: Period, F <: Financials] {
  val repository: PropertiesRepository

  val propertyOps: PropertyPeriodOps[P, F]

  def updatePeriod(nino: Nino, periodId: PeriodId, period: F): Future[Boolean] = {
    repository.retrieve(nino).flatMap {
      case Some(property) if propertyOps.periodExists(periodId, property) =>
        repository.update(nino, propertyOps.update(periodId, period, property))
      case _ => Future.successful(false)
    }
  }
  
  def retrieveAllPeriods(nino: Nino): Future[Option[Seq[PeriodSummary]]] = {
    repository.retrieve(nino).map {
      case Some(properties) => {
        val bucket = propertyOps.periods(properties)

        Some(bucket.map { case (k, v) => PeriodSummary(k, v.from, v.to) }.toSeq.sorted)
      }
      case None => None
    }
  }
}

object OtherPropertiesPeriodService extends PropertiesPeriodService[Other.Properties, Other.Financials] {
  override val repository: PropertiesRepository = PropertiesRepository()
  override val propertyOps: PropertyPeriodOps[Other.Properties, Other.Financials] =
    implicitly[PropertyPeriodOps[Other.Properties, Other.Financials]]
}

object FHLPropertiesPeriodService extends PropertiesPeriodService[FHL.Properties, FHL.Financials] {
  override val repository: PropertiesRepository = PropertiesRepository()
  override val propertyOps: PropertyPeriodOps[FHL.Properties, FHL.Financials] =
    implicitly[PropertyPeriodOps[FHL.Properties, FHL.Financials]]
}
