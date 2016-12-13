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
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.{Benefit, Income}
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.repositories.SummaryRepository

import scala.concurrent.ExecutionContext.Implicits.global

class BenefitsRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val mongoRepository = new BenefitsMongoRepository
  private val summariesMap: Map[JsonMarshaller[_], SummaryRepository[_]] = Map(Income -> mongoRepository.BenefitRepository)

  override def beforeEach() {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

  val nino = NinoGenerator().nextNino()

  def benefit(): Benefit = Benefit.example()

  "delete by Id" should {
    "return true when benefit source is deleted" in {
      val source = benefit()
      val id = await(mongoRepository.create(nino, taxYear, source))
      val result = await(mongoRepository.delete(nino, taxYear, id))

      result shouldBe true
    }

    "return false when benefit source is not deleted" in {
      val source = benefit()
      val id = await(mongoRepository.create(nino, taxYear, source))
      val result = await(mongoRepository.delete(NinoGenerator().nextNino(), taxYear, id))

      result shouldBe false
    }
  }

  "delete by utr and taxYear" should {
    "delete  all benefit sources for utr/tax year" in {
      for {
        n <- 1 to 10
        source = benefit()
        id = await(mongoRepository.create(nino, taxYear, source))
      } yield source.copy(id = Some(id))


      await(mongoRepository.delete(nino, taxYear))

      val found: Seq[_] = await(mongoRepository.list(nino, taxYear))

      found shouldBe empty
    }

    "not delete benefit sources for different utr" in {
      val nino2 = NinoGenerator().nextNino()
      await(mongoRepository.create(nino, taxYear, benefit()))
      val source2 = await(mongoRepository.create(nino2, taxYear, benefit()))

      await(mongoRepository.delete(nino, taxYear))
      val found: Seq[Benefit] = await(mongoRepository.list(nino2, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source2)
    }
  }


  "list" should {
    "retrieve all benefit sources for utr/tax year" in {
      val sources = for {
        n <- 1 to 10
        source = benefit()
        id = await(mongoRepository.create(nino, taxYear, source))
      } yield source.copy(id = Some(id))


      val found: Seq[_] = await(mongoRepository.list(nino, taxYear))

      found should contain theSameElementsAs sources
    }

    "not include benefit sources for different utr" in {
      val source1 = await(mongoRepository.create(nino, taxYear, benefit()))
      await(mongoRepository.create(NinoGenerator().nextNino(), taxYear, benefit()))

      val found: Seq[Benefit] = await(mongoRepository.list(nino, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source1)
    }
  }

  "update" should {

    "return false when the benefit source does not exist" in {
      val result = await(mongoRepository.update(nino, taxYear, UUID.randomUUID().toString, benefit()))
      result shouldEqual false
    }

    "update last modified" in {
      val source = benefit()
      val sourceId = await(mongoRepository.create(nino, taxYear, source))
      val found = await(mongoRepository.findById(BSONObjectID(sourceId)))
      await(mongoRepository.update(nino, taxYear, sourceId, source))

      val found1 = await(mongoRepository.findById(BSONObjectID(sourceId)))

      // Added the equals clauses as it was failing locally once, can fail if the test runs faster and has the same time for create and update
      found1.get.lastModifiedDateTime.isEqual(found.get.lastModifiedDateTime) || found1.get.lastModifiedDateTime.isAfter(found.get.lastModifiedDateTime) shouldBe true
    }
  }

  def cast[A](a: Any): A = a.asInstanceOf[A]

  "create summary" should {
    "add a summary to an empty list when source exists and return id" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(mongoRepository.create(nino, taxYear, benefit()))
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
        val sourceId = await(mongoRepository.create(nino, taxYear, benefit()))
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
        val sourceId = await(mongoRepository.create(nino, taxYear, benefit()))
        await(repo.findById(nino, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }

    "return the summary if found" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(mongoRepository.create(nino, taxYear, benefit()))
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
        val sourceId = await(mongoRepository.create(nino, taxYear, benefit()))
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
        val sourceId = await(mongoRepository.create(nino, taxYear, benefit()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary))).get
        await(repo.delete(nino, taxYear, sourceId, summaryId)) shouldEqual true
      }
    }

    "only delete the specified summary" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(mongoRepository.create(nino, taxYear, benefit()))
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
        val sourceId = await(mongoRepository.create(nino, taxYear, benefit()))
        await(repo.delete(nino, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual false
      }
    }
  }

  "update income" should {
    "return true when the income has been updated" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(mongoRepository.create(nino, taxYear, benefit()))
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
        val sourceId = await(mongoRepository.create(nino, taxYear, benefit()))
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
      val sourceId = await(mongoRepository.create(nino, taxYear, benefit()))
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.update(nino, taxYear, sourceId, BSONObjectID.generate.stringify, cast(summaryItem.example()))) shouldEqual false
      }
    }
  }
}
