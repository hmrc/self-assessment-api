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

package uk.gov.hmrc.selfassessmentapi.repositories.live

import java.util.UUID

import org.scalatest.BeforeAndAfterEach
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonMarshaller
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.FurnishedHolidayLetting
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType.EEA
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings._
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{FurnishedHolidayLettings, FurnishedHolidayLettingsIncomeSummary}
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepository, SummaryRepository}

import scala.concurrent.ExecutionContext.Implicits.global

class FurnishedHolidayLettingsRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val mongoRepository = new FurnishedHolidayLettingsMongoRepository
  private val furnishedHolidayLettingsRepository: SourceRepository[FurnishedHolidayLetting] = mongoRepository
  private val summariesMap: Map[JsonMarshaller[_], SummaryRepository[_]] = Map(Income -> mongoRepository.IncomeRepository,
    Expense -> mongoRepository.ExpenseRepository, BalancingCharge -> mongoRepository.BalancingChargeRepository,
    PrivateUseAdjustment -> mongoRepository.PrivateUseAdjustmentRepository)


  override def beforeEach() {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

  val nino = NinoGenerator().nextNino()

  def furnishedHolidayLetting(): FurnishedHolidayLetting = FurnishedHolidayLetting.example()

  "round trip" should {
    "create and retrieve using generated id" in {
      val source = furnishedHolidayLetting()
      val id = await(furnishedHolidayLettingsRepository.create(nino, taxYear, source))
      val found: FurnishedHolidayLetting = await(furnishedHolidayLettingsRepository.findById(nino, taxYear, id)).get

      found.propertyLocation shouldBe source.propertyLocation
    }
  }

  "delete by Id" should {
    "return true when furnished holiday letting is deleted" in {
      val source = furnishedHolidayLetting()
      val id = await(furnishedHolidayLettingsRepository.create(nino, taxYear, source))
      val result = await(furnishedHolidayLettingsRepository.delete(nino, taxYear, id))

      result shouldBe true
    }

    "return false when furnished holiday letting is not deleted" in {
      val source = furnishedHolidayLetting()
      val id = await(furnishedHolidayLettingsRepository.create(nino, taxYear, source))
      val result = await(furnishedHolidayLettingsRepository.delete(NinoGenerator().nextNino(), taxYear, id))

      result shouldBe false
    }
  }

  "delete by utr and taxYear" should {
    "delete all furnished holiday lettings for utr/tax year" in {
      for {
        n <- 1 to 10
        source = furnishedHolidayLetting()
        id = await(furnishedHolidayLettingsRepository.create(nino, taxYear, source))
      } yield source.copy(id = Some(id))


      await(furnishedHolidayLettingsRepository.delete(nino, taxYear))

      val found: Seq[FurnishedHolidayLetting] = await(furnishedHolidayLettingsRepository.list(nino, taxYear))

      found shouldBe empty
    }

    "not delete furnished holiday letting for different utr" in {
      val nino2 = NinoGenerator().nextNino()
      await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
      val source2 = await(furnishedHolidayLettingsRepository.create(nino2, taxYear, furnishedHolidayLetting()))

      await(furnishedHolidayLettingsRepository.delete(nino, taxYear))
      val found: Seq[FurnishedHolidayLetting] = await(furnishedHolidayLettingsRepository.list(nino2, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source2)
    }
  }


  "list" should {
    "retrieve all furnished holiday letting for utr/tax year" in {
      val sources = for {
        n <- 1 to 10
        source = furnishedHolidayLetting()
        id = await(furnishedHolidayLettingsRepository.create(nino, taxYear, source))
      } yield source.copy(id = Some(id))


      val found: Seq[FurnishedHolidayLetting] = await(furnishedHolidayLettingsRepository.list(nino, taxYear))

      found should contain theSameElementsAs sources
    }

    "not include furnished holiday letting for different utr" in {
      val source1 = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
      await(furnishedHolidayLettingsRepository.create(NinoGenerator().nextNino(), taxYear, furnishedHolidayLetting()))

      val found: Seq[FurnishedHolidayLetting] = await(furnishedHolidayLettingsRepository.list(nino, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source1)
    }
  }

  "update" should {
    def verifyUpdate(original: FurnishedHolidayLetting, updated: FurnishedHolidayLetting) = {
      val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, original))
      val result = await(furnishedHolidayLettingsRepository.update(nino, taxYear, sourceId, updated))
      result shouldEqual true

      val found = await(furnishedHolidayLettingsRepository.findById(nino, taxYear, sourceId))
      found shouldEqual Some(updated.copy(id = Some(sourceId)))

    }
    "return true when the furnished holiday letting exists and has been updated" in {
      val source = furnishedHolidayLetting()

      val allowances = Allowances(
        capitalAllowance = Some(BigDecimal(10.00)))

      val adjustments = Adjustments(
        lossBroughtForward = Some(BigDecimal(10.00)))

      val updatedSource = source.copy(
        propertyLocation = EEA,
        allowances = Some(allowances),
        adjustments = Some(adjustments)
      )

      verifyUpdate(source, updatedSource)
    }

    "set allowances to None if not provided" in {
      val source = furnishedHolidayLetting()

      val updatedSource = source.copy(
        allowances = None
      )

      verifyUpdate(source, updatedSource)
    }

    "set adjustments to None if not provided" in {
      val source = furnishedHolidayLetting()

      val updatedSource = source.copy(
        adjustments = None
      )

      verifyUpdate(source, updatedSource)
    }

    "return false when the furnished holiday letting does not exist" in {
      val result = await(furnishedHolidayLettingsRepository.update(nino, taxYear, UUID.randomUUID().toString, furnishedHolidayLetting()))
      result shouldEqual false
    }

    "not remove incomes" in {
      val source = FurnishedHolidayLettings.create(nino, taxYear, furnishedHolidayLetting()).copy(incomes = Seq(FurnishedHolidayLettingsIncomeSummary(BSONObjectID.generate.stringify,  10)))
      await(mongoRepository.insert(source))
      val found = await(mongoRepository.findById(nino, taxYear, source.sourceId)).get
      await(furnishedHolidayLettingsRepository.update(nino, taxYear, source.sourceId, found))

      val found1 = await(mongoRepository.findById(source.id))

      found1.get.incomes should not be empty
    }

    "update last modified" in {
      val source = furnishedHolidayLetting()
      val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, source))
      val found = await(mongoRepository.findById(BSONObjectID(sourceId)))
      await(furnishedHolidayLettingsRepository.update(nino, taxYear, sourceId, source))

      val found1 = await(mongoRepository.findById(BSONObjectID(sourceId)))

      // Added the equals clauses as it was failing locally once, can fail if the test runs faster and has the same time for create and update
      found1.get.lastModifiedDateTime.isEqual(found.get.lastModifiedDateTime) || found1.get.lastModifiedDateTime.isAfter(found.get.lastModifiedDateTime) shouldBe true
    }
  }

  def cast[A](a: Any): A = a.asInstanceOf[A]

  "create summary" should {
    "add a summary to an empty list when source exists and return id" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary)))

        summaryId.isDefined shouldEqual true
        val dbSummaries = await(repo.list(nino, taxYear, sourceId))

        val found = dbSummaries.get
        found.headOption shouldEqual Some(summaryItem.example(id = summaryId))
      }
    }

    "add a summary to the existing list when source exists and return id" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
        val summary = summaryItem.example()
        val summary1 = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary)))
        val summaryId1 = await(repo.create(nino, taxYear, sourceId, cast(summary1)))

        val summaries = await(repo.list(nino, taxYear, sourceId))

        val found = summaries.get
        found should contain theSameElementsAs Seq(summaryItem.example(id = summaryId), summaryItem.example(id = summaryId1))
      }
    }

    "return none when source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, BSONObjectID.generate.stringify, cast(summary)))
        summaryId shouldEqual None
      }
    }
  }

  "find summary by id" should {
    "return none if the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.findById(nino, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }

    "return none if the summary does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
        await(repo.findById(nino, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }

    "return the summary if found" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary))).get
        val found = await(repo.findById(nino, taxYear, sourceId, summaryId))

        found shouldEqual Some(summaryItem.example(id = Some(summaryId)))
      }
    }
  }

  "list summaries" should {
    "return empty list when source has no summaries" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
        await(repo.list(nino, taxYear, sourceId)) shouldEqual Some(Seq.empty)
      }
    }

    "return none when source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.list(nino, taxYear, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }
  }

  "delete summary" should {
    "return true when the summary has been deleted" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary))).get
        await(repo.delete(nino, taxYear, sourceId, summaryId)) shouldEqual true
      }
    }

    "only delete the specified summary" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary))).get
        val summaryId1 = await(repo.create(nino, taxYear, sourceId, cast(summary)))
        await(repo.delete(nino, taxYear, sourceId, summaryId))

        val found = await(repo.list(nino, taxYear, sourceId)).get
        found.size shouldEqual 1
        found.head shouldEqual summaryItem.example(id = summaryId1)
      }
    }

    "return false when the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.delete(nino, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual false
      }
    }

    "return false when the summary does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
        await(repo.delete(nino, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual false
      }
    }
  }

  "update income" should {
    "return true when the income has been updated" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary))).get

        val summaryToUpdate = summaryItem.example()
        await(repo.update(nino, taxYear, sourceId, summaryId, cast(summaryToUpdate))) shouldEqual true

        val found = await(repo.findById(nino, taxYear, sourceId, summaryId))

        found shouldEqual Some(summaryItem.example(id = Some(summaryId)))
      }
    }

    "only update the specified income" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
        val summary1 = summaryItem.example()
        val summaryId1 = await(repo.create(nino, taxYear, sourceId, cast(summary1))).get
        val summary2 = summaryItem.example()
        val summaryId2 = await(repo.create(nino, taxYear, sourceId, cast(summary2))).get

        val summaryToUpdate = summaryItem.example()
        await(repo.update(nino, taxYear, sourceId, summaryId2, cast(summaryToUpdate))) shouldEqual true

        val found = await(repo.list(nino, taxYear, sourceId)).get

        found should contain theSameElementsAs Seq(summaryItem.example(id = Some(summaryId1)), summaryItem.example(id = Some(summaryId2)))
      }
    }

    "return false when the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.update(nino, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify, cast(summaryItem.example()))) shouldEqual false
      }
    }

    "return false when the income does not exist" in {
      val sourceId = await(furnishedHolidayLettingsRepository.create(nino, taxYear, furnishedHolidayLetting()))
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.update(nino, taxYear, sourceId, BSONObjectID.generate.stringify, cast(summaryItem.example()))) shouldEqual false
      }
    }
  }


}
