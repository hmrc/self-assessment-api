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

import org.joda.time.{DateTimeZone, LocalDate}
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.domain.Properties
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{PropertiesAnnualSummary, PropertiesPeriod, PropertiesPeriodicData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertiesService extends PeriodService[PropertyId, PropertiesPeriod, Properties, PropertiesPeriodicData]
  with AnnualSummaryService[PropertiesAnnualSummary, Properties] {

  private val repository = PropertiesRepository()
  override val periodRepository = repository
  override val annualSummaryRepository = repository

  def create(nino: Nino, props: properties.Properties): Future[Either[Error, Boolean]] = {
    val properties = Properties(BSONObjectID.generate,
      LocalDate.now(DateTimeZone.UTC), nino, props.accountingType, Map.empty, Map.empty)

    periodRepository.create(properties).map(Right(_)) recover {
      case e: DatabaseException if e.code.contains(11000) =>  // i.e. Duplicate key exception.
        Left(
          Error(ErrorCode.ALREADY_EXISTS.toString, s"A property business already exists", ""))
    }
  }

  override def retrieveAnnualSummary(id: SourceId, taxYear: TaxYear, nino: Nino): Future[Option[PropertiesAnnualSummary]] = {
    annualSummaryRepository.retrieve(id, nino).map {
      case Some(resource) => resource.annualSummary(taxYear).orElse(Some(PropertiesAnnualSummary(None, None, None, None, None)))
      case None => None
    }
  }

  def updateAnnualSummary(nino: Nino, propType: PropertyId, taxYear: TaxYear, summary: PropertiesAnnualSummary): Future[Boolean] = {
    periodRepository.retrieve(propType, nino).flatMap {
      case Some(properties) => periodRepository.update(propType, nino, properties.copy(annualSummaries = properties.annualSummaries.updated(taxYear, summary)))
      case None => throw new RuntimeException("Could not persist Properties to the database. Is the database available?")
    }
  }
}

object PropertiesService {
  def apply(): PropertiesService = new PropertiesService
}
