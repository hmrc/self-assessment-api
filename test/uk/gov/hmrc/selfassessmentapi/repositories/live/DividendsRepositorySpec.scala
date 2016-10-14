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
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend.{Dividend, DividendIncome}

import scala.concurrent.ExecutionContext.Implicits.global

class DividendsRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val mongoRepository = new DividendMongoRepository
  private val unearnedIncomeMongoRepository = mongoRepository
  private val summariesMap = Map(DividendIncome -> mongoRepository.DividendIncomeRepository)

  override def beforeEach() {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

  val saUtr = generateSaUtr()

  def unearnedIncome() = Dividend.example()

  "delete by Id" should {
    "return true when unearned income is deleted" in {
      val source = unearnedIncome()
      val id = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, source))
      val result = await(unearnedIncomeMongoRepository.delete(saUtr, taxYear, id))

      result shouldBe true
    }

    "return false when unearned is not deleted" in {
      val source = unearnedIncome()
      val id = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, source))
      val result = await(unearnedIncomeMongoRepository.delete(generateSaUtr(), taxYear, id))

      result shouldBe false
    }
  }

  "delete by utr and taxYear" should {
    "delete  all unearned incomes for utr/tax year" in {
      for {
        n <- 1 to 10
        source = unearnedIncome()
        id = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))


      await(unearnedIncomeMongoRepository.delete(saUtr, taxYear))

      val found = await(unearnedIncomeMongoRepository.list(saUtr, taxYear))

      found shouldBe empty
    }

    "not delete unearned incomes for different utr" in {
      val saUtr2: SaUtr = generateSaUtr()
      await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
      val source2 = await(unearnedIncomeMongoRepository.create(saUtr2, taxYear, unearnedIncome()))

      await(unearnedIncomeMongoRepository.delete(saUtr, taxYear))
      val found = await(unearnedIncomeMongoRepository.list(saUtr2, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source2)
    }
  }


  "list" should {
    "retrieve all unearned incomes for utr/tax year" in {
      val sources = for {
        n <- 1 to 10
        source = unearnedIncome()
        id = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))


      val found = await(unearnedIncomeMongoRepository.list(saUtr, taxYear))

      found should contain theSameElementsAs sources
    }

    "not include unearned incomes for different utr" in {
      val source1 = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
      await(unearnedIncomeMongoRepository.create(generateSaUtr(), taxYear, unearnedIncome()))

      val found = await(unearnedIncomeMongoRepository.list(saUtr, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source1)
    }
  }

  "update" should {

    "return false when the unearned income does not exist" in {
      val result = await(unearnedIncomeMongoRepository.update(saUtr, taxYear, UUID.randomUUID().toString, unearnedIncome()))
      result shouldEqual false
    }

    "update last modified" in {
      val source = unearnedIncome()
      val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, source))
      val found = await(mongoRepository.findById(BSONObjectID(sourceId)))
      await(unearnedIncomeMongoRepository.update(saUtr, taxYear, sourceId, source))

      val found1 = await(mongoRepository.findById(BSONObjectID(sourceId)))

      // Added the equals clauses as it was failing locally once, can fail if the test runs faster and has the same time for create and update
      found1.get.lastModifiedDateTime.isEqual(found.get.lastModifiedDateTime) || found1.get.lastModifiedDateTime.isAfter(found.get.lastModifiedDateTime) shouldBe true
    }
  }

  def cast[A](a: Any): A = a.asInstanceOf[A]

  "create summary" should {
    "add a summary to an empty list when source exists and return id" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary)))

        summaryId.isDefined shouldEqual true
        val dbSummaries = await(repo.list(saUtr, taxYear, sourceId))

        dbSummaries.get.headOption shouldEqual Some(summaryItem.example(id = summaryId))
      }
    }

    "add a summary to the existing list when source exists and return id" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
        val summary = summaryItem.example()
        val summary1 = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary)))
        val summaryId1 = await(repo.create(saUtr, taxYear, sourceId, cast(summary1)))

        val summaries = await(repo.list(saUtr, taxYear, sourceId))

        summaries.get should contain theSameElementsAs Seq(summaryItem.example(id = summaryId), summaryItem.example(id = summaryId1))
      }
    }

    "return none when source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, BSONObjectID.generate.stringify, cast(summary)))
        summaryId shouldEqual None
      }
    }
  }

  "find summary by id" should {
    "return none if the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.findById(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }

    "return none if the summary does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
        await(repo.findById(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }

    "return the summary if found" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary))).get
        await(repo.findById(saUtr, taxYear, sourceId, summaryId)) shouldEqual Some(summaryItem.example(id = Some(summaryId)))
      }
    }
  }

  "list summaries" should {
    "return empty list when source has no summaries" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
        await(repo.list(saUtr, taxYear, sourceId)) shouldEqual Some(Seq.empty)
      }
    }

    "return none when source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.list(saUtr, taxYear, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }
  }

  "delete summary" should {
    "return true when the summary has been deleted" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary))).get
        await(repo.delete(saUtr, taxYear, sourceId, summaryId)) shouldEqual true
      }
    }

    "only delete the specified summary" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary))).get
        val summaryId1 = await(repo.create(saUtr, taxYear, sourceId, cast(summary)))
        await(repo.delete(saUtr, taxYear, sourceId, summaryId))

        val found = await(repo.list(saUtr, taxYear, sourceId)).get
        found.size shouldEqual 1
        found.head shouldEqual summaryItem.example(id = summaryId1)
      }
    }

    "return false when the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.delete(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual false
      }
    }

    "return false when the summary does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
        await(repo.delete(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual false
      }
    }
  }

  "update income" should {
    "return true when the income has been updated" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary))).get

        val summaryToUpdate = summaryItem.example()
        await(repo.update(saUtr, taxYear, sourceId, summaryId, cast(summaryToUpdate))) shouldEqual true

        val found = await(repo.findById(saUtr, taxYear, sourceId, summaryId))

        found shouldEqual Some(summaryItem.example(id = Some(summaryId)))
      }
    }

    "only update the specified income" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
        val summary1 = summaryItem.example()
        val summaryId1 = await(repo.create(saUtr, taxYear, sourceId, cast(summary1))).get
        val summary2 = summaryItem.example()
        val summaryId2 = await(repo.create(saUtr, taxYear, sourceId, cast(summary2))).get

        val summaryToUpdate = summaryItem.example()
        await(repo.update(saUtr, taxYear, sourceId, summaryId2, cast(summaryToUpdate))) shouldEqual true

        val found = await(repo.list(saUtr, taxYear, sourceId)).get

        found should contain theSameElementsAs Seq(summaryItem.example(id = Some(summaryId1)), summaryItem.example(id = Some(summaryId2)))
      }
    }

    "return false when the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.update(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify, cast(summaryItem.example()))) shouldEqual false
      }
    }

    "return false when the income does not exist" in {
      val sourceId = await(unearnedIncomeMongoRepository.create(saUtr, taxYear, unearnedIncome()))
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.update(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify, cast(summaryItem.example()))) shouldEqual false
      }
    }
  }


}
