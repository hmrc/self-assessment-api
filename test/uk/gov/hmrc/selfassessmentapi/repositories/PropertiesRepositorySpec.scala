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

package uk.gov.hmrc.selfassessmentapi.repositories

import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfterEach
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.domain.Properties
import uk.gov.hmrc.selfassessmentapi.resources.models.{AccountingType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{Allowances, PropertiesAnnualSummary, PropertyType}

import scala.concurrent.ExecutionContext.Implicits.global

class PropertiesRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val repo = new PropertiesRepository
  private val nino = NinoGenerator().nextNino()
  private val propertyId = ""

  override def beforeEach() = {
    await(repo.drop)
    await(repo.ensureIndexes)
  }

  "create" should {
    "persist a properties object" in {
      val properties = Properties(BSONObjectID.generate, LocalDate.now, nino, AccountingType.CASH, Map.empty, Map.empty)
      await(repo.create(properties))

      val result = await(repo.retrieve(propertyId, nino)).get
      result.nino shouldBe nino
      result.periods shouldBe empty
    }
  }

  "update" should {
    "update a properties object" in {
      val properties = Properties(BSONObjectID.generate, LocalDate.now, nino, AccountingType.CASH, Map.empty, Map.empty)
      await(repo.create(properties))

      await(repo.retrieve(propertyId, nino)) shouldBe Some(properties)

      val updatedProperties = properties.copy(annualSummaries =
        Map(TaxYear("2016-17") -> PropertiesAnnualSummary(Some(Allowances(annualInvestmentAllowance = Some(50.25))), None, None, None, None)))
      await(repo.update(propertyId, nino, updatedProperties))
      await(repo.retrieve(propertyId, nino)) shouldBe Some(updatedProperties)
    }
  }
}
