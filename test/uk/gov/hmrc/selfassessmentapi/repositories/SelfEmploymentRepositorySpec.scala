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

import org.joda.time.{DateTimeZone, LocalDate}
import org.scalatest.BeforeAndAfterEach
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment._

import scala.concurrent.ExecutionContext.Implicits.global

class SelfEmploymentRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {
  private val ninoGenerator = NinoGenerator()
  private val nino = ninoGenerator.nextNino()
  private val repo = new SelfEmploymentsRepository
  private val id = BSONObjectID.generate
  private val selfEmployment = SelfEmployment(id, id.stringify, nino, LocalDate.now(DateTimeZone.UTC),
    AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")), AccountingType.CASH, LocalDate.now(DateTimeZone.UTC))

  override def beforeEach(): Unit = {
    await(repo.drop)
    await(repo.ensureIndexes)
  }

  "create" should {
    "create a self employment with a non-null id" in {

      await(repo.create(selfEmployment))

      val result = await(repo.retrieve(id.stringify, nino)).get

      result.commencementDate shouldBe selfEmployment.commencementDate
    }
  }

  "update" should {
    "fail if a self-employment does not already exist" in {
      await(repo.update("invalidSourceId", ninoGenerator.nextNino(), selfEmployment)) shouldBe false
    }

    "overwrite existing fields in self-employment with new data provided by the user" in {
      await(repo.create(selfEmployment))
      val updatedSelfEmployment = selfEmployment.copy(commencementDate = now.toLocalDate.plusDays(1))

      await(repo.update(id.stringify, nino, updatedSelfEmployment)) shouldBe true

      val result = await(repo.retrieve(id.stringify, nino)).get
      result.accountingPeriod shouldBe updatedSelfEmployment.accountingPeriod
      result.accountingType shouldBe updatedSelfEmployment.accountingType
      result.commencementDate shouldBe updatedSelfEmployment.commencementDate
    }

    "return true when updating an annual summaries" in {
      val summary = SelfEmploymentAnnualSummary(Some(Allowances.example), Some(Adjustments.example))

      await(repo.create(selfEmployment))

      await(repo.update(id.stringify, nino, selfEmployment.copy(annualSummaries = Map(TaxYear("2016-17") -> summary)))) shouldBe true
      val updatedSelfEmployment = await(repo.retrieve(id.stringify, nino)).get

      updatedSelfEmployment.annualSummaries.size shouldBe 1
      updatedSelfEmployment.annualSummary(TaxYear("2016-17")) shouldBe Some(summary)
    }

    "return true when updating periods" in {
      await(repo.create(selfEmployment))
      val period = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1),
        SelfEmploymentPeriodicData(Map(IncomeType.Turnover -> Income(500.00)), Map.empty))

      await(repo.update(id.stringify, nino, selfEmployment.copy(periods = Map("1" -> period)))) shouldBe true
      val updatedSelfEmployment = await(repo.retrieve(id.stringify, nino)).get
      updatedSelfEmployment.periods.size shouldBe 1
      updatedSelfEmployment.period("1") shouldBe Some(period)
    }
  }

  "retrieve" should {
    "return None if the self-employment source does not exist" in {
      await(repo.retrieve("invalidSourceId", ninoGenerator.nextNino())) shouldBe None
    }
  }

  "retrieveAll" should {
    "return an empty sequence if the user has no self-employment sources" in {
      await(repo.retrieveAll(nino)) shouldBe Seq.empty[SelfEmployment]
    }

    "return a sequence of self employments" in {
      val id2 = BSONObjectID.generate
      val selfEmploymentTwo = SelfEmployment(id2, id2.stringify, nino, LocalDate.now(DateTimeZone.UTC),
        AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")), AccountingType.CASH, LocalDate.now(DateTimeZone.UTC))

      await(repo.create(selfEmployment))
      await(repo.create(selfEmploymentTwo))

      val result = await(repo.retrieveAll(nino))
      val (first :: second :: _) = result

      result should have length 2

      first.sourceId shouldBe id.stringify
      first.nino shouldBe nino
      first.accountingPeriod shouldBe selfEmployment.accountingPeriod
      first.accountingType shouldBe selfEmployment.accountingType
      first.commencementDate shouldBe selfEmployment.commencementDate

      second.sourceId shouldBe id2.stringify
      second.nino shouldBe nino
      second.accountingPeriod shouldBe selfEmploymentTwo.accountingPeriod
      second.accountingType shouldBe selfEmploymentTwo.accountingType
      second.commencementDate shouldBe selfEmploymentTwo.commencementDate
    }
  }

}
