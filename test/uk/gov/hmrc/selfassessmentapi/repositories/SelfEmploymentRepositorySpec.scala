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

package uk.gov.hmrc.selfassessmentapi.repositories

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment._

import scala.concurrent.ExecutionContext.Implicits.global

class SelfEmploymentRepositorySpec extends MongoEmbeddedDatabase {
  private val ninoGenerator = NinoGenerator()
  private val nino = ninoGenerator.nextNino()
  private val repo = new SelfEmploymentsRepository
  private val id = BSONObjectID.generate

  private def createSelfEmployment(nino: Nino, id: BSONObjectID = BSONObjectID.generate): SelfEmployment = {
    val accountingPeriod = AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02"))

    SelfEmployment(id, id.stringify, nino, DateTime.now(DateTimeZone.UTC),
      accountingPeriod, AccountingType.CASH, LocalDate.now, "Acme Ltd.",
      "Boxes made of corrugated cardboard (manufacture)", "1 Acme Rd.", Some("London"), Some("Greater London"), Some("United Kingdom"),
      "A9 9AA", Map.empty, Map.empty)
  }

  "create" should {
    "create a self employment with a non-null id" in {
      val se = createSelfEmployment(nino, id)

      await(repo.create(se))

      val result = await(repo.retrieve(id.stringify, nino)).get

      result.commencementDate shouldBe se.commencementDate
    }
  }

  "update" should {
    "fail if a self-employment does not already exist" in {
      await(repo.update("invalidSourceId", nino, createSelfEmployment(nino))) shouldBe false
    }

    "overwrite existing fields in self-employment with new data provided by the user" in {
      val se = createSelfEmployment(nino, id)

      await(repo.create(se))
      val updatedSelfEmployment = se.copy(commencementDate = now.toLocalDate.plusDays(1))

      await(repo.update(id.stringify, nino, updatedSelfEmployment)) shouldBe true

      val result = await(repo.retrieve(id.stringify, nino)).get
      result.accountingPeriod shouldBe updatedSelfEmployment.accountingPeriod
      result.accountingType shouldBe updatedSelfEmployment.accountingType
      result.commencementDate shouldBe updatedSelfEmployment.commencementDate
    }

    "return true when updating an annual summaries" in {
      val se = createSelfEmployment(nino, id)
      val summary = SelfEmploymentAnnualSummary(Some(Allowances.example), Some(Adjustments.example))

      await(repo.create(se))

      await(repo.update(id.stringify, nino, se.copy(annualSummaries = Map(TaxYear("2016-17") -> summary)))) shouldBe true
      val updatedSelfEmployment = await(repo.retrieve(id.stringify, nino)).get

      updatedSelfEmployment.annualSummaries.size shouldBe 1
      updatedSelfEmployment.annualSummary(TaxYear("2016-17")) shouldBe summary
    }

    "return true when updating periods" in {
      val se = createSelfEmployment(nino, id)

      await(repo.create(se))
      val period = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1),
        SelfEmploymentPeriodicData(Map(IncomeType.Turnover -> Income(500.00, None)), Map.empty))

      await(repo.update(id.stringify, nino, se.copy(periods = Map("1" -> period)))) shouldBe true
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
      val se = createSelfEmployment(nino)
      val selfEmploymentTwo = createSelfEmployment(nino)

      await(repo.create(se))
      await(repo.create(selfEmploymentTwo))

      val result = await(repo.retrieveAll(nino))

      result should contain theSameElementsAs Seq(se, selfEmploymentTwo)
    }
  }

  "deleteAllBeforeDate" should {
    "delete all records older than the provided DateTime object" in {
      val selfEmploymentToKeepOne = createSelfEmployment(nino).copy(lastModifiedDateTime = DateTime.now(DateTimeZone.UTC).plusDays(1))
      val selfEmploymentToKeepTwo = createSelfEmployment(nino)
      val selfEmploymentToRemoveOne = createSelfEmployment(nino).copy(lastModifiedDateTime = DateTime.now(DateTimeZone.UTC).minusDays(1))


      await(repo.create(selfEmploymentToKeepOne))
      await(repo.create(selfEmploymentToRemoveOne))
      await(repo.create(selfEmploymentToKeepTwo))
      await(repo.deleteAllBeforeDate(DateTime.now(DateTimeZone.UTC).minusHours(1))) shouldBe 1
      await(repo.retrieveAll(nino)) should contain theSameElementsAs Seq(selfEmploymentToKeepOne, selfEmploymentToKeepTwo)
    }
  }

}
