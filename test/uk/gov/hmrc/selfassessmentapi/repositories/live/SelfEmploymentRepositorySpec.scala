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
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonMarshaller
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{SelfEmployment, SelfEmploymentIncomeSummary}
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepository, SummaryRepository}

import scala.concurrent.ExecutionContext.Implicits.global

class SelfEmploymentRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val mongoRepository = new SelfEmploymentMongoRepository
  private val selfEmploymentRepository: SourceRepository[selfemployment.SelfEmployment] = mongoRepository
  private val summariesMap: Map[JsonMarshaller[_], SummaryRepository[_]] = Map(
    Income -> mongoRepository.IncomeRepository,
    Expense -> mongoRepository.ExpenseRepository,
    BalancingCharge -> mongoRepository.BalancingChargeRepository,
    GoodsAndServicesOwnUse -> mongoRepository.GoodsAndServicesOwnUseRepository
  )

  override def beforeEach() {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

  val saUtr = generateSaUtr()

  def selfEmployment(): selfemployment.SelfEmployment = selfemployment.SelfEmployment.example()

  "round trip" should {
    "create and retrieve using generated id" in {
      val source = selfEmployment()
      val id = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      val found: selfemployment.SelfEmployment = await(selfEmploymentRepository.findById(saUtr, taxYear, id)).get

      found.commencementDate shouldBe source.commencementDate
    }
  }

  "delete by Id" should {
    "return true when self employment is deleted" in {
      val source = selfEmployment()
      val id = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      val result = await(selfEmploymentRepository.delete(saUtr, taxYear, id))

      result shouldBe true
    }

    "return false when self employment is not deleted" in {
      val source = selfEmployment()
      val id = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      val result = await(selfEmploymentRepository.delete(generateSaUtr(), taxYear, id))

      result shouldBe false
    }
  }

  "delete by utr and taxYear" should {
    "delete  all self employments for utr/tax year" in {
      for {
        n <- 1 to 10
        source = selfEmployment()
        id = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))

      await(selfEmploymentRepository.delete(saUtr, taxYear))

      val found: Seq[selfemployment.SelfEmployment] = await(selfEmploymentRepository.list(saUtr, taxYear))

      found shouldBe empty
    }

    "not delete self employments for different utr" in {
      val saUtr2: SaUtr = generateSaUtr()
      await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      val source2 = await(selfEmploymentRepository.create(saUtr2, taxYear, selfEmployment()))

      await(selfEmploymentRepository.delete(saUtr, taxYear))
      val found: Seq[selfemployment.SelfEmployment] = await(selfEmploymentRepository.list(saUtr2, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source2)
    }
  }

  "list" should {
    "retrieve all self employments for utr/tax year" in {
      val sources = for {
        n <- 1 to 10
        source = selfEmployment()
        id = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))

      val found: Seq[selfemployment.SelfEmployment] = await(selfEmploymentRepository.list(saUtr, taxYear))

      found should contain theSameElementsAs sources
    }

    "not include self employments for different utr" in {
      val source1 = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      await(selfEmploymentRepository.create(generateSaUtr(), taxYear, selfEmployment()))

      val found: Seq[selfemployment.SelfEmployment] = await(selfEmploymentRepository.list(saUtr, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source1)
    }
  }

  "update" should {
    def verifyUpdate(original: selfemployment.SelfEmployment, updated: selfemployment.SelfEmployment) = {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, original))
      val result = await(selfEmploymentRepository.update(saUtr, taxYear, sourceId, updated))
      result shouldEqual true

      val found = await(selfEmploymentRepository.findById(saUtr, taxYear, sourceId))
      found shouldEqual Some(updated.copy(id = Some(sourceId)))

    }
    "return true when the self employment exists and has been updated" in {
      val source = selfEmployment()

      val allowances = Allowances(annualInvestmentAllowance = Some(BigDecimal(10.00)),
                                  capitalAllowanceMainPool = Some(BigDecimal(20.00)),
                                  capitalAllowanceSpecialRatePool = Some(BigDecimal(30.00)),
                                  businessPremisesRenovationAllowance = Some(BigDecimal(50.00)),
                                  enhancedCapitalAllowance = Some(BigDecimal(60.00)),
                                  allowancesOnSales = Some(BigDecimal(70.00)))

      val adjustments = Adjustments(includedNonTaxableProfits = Some(BigDecimal(10.00)),
                                    basisAdjustment = Some(BigDecimal(20.00)),
                                    overlapReliefUsed = Some(BigDecimal(30.00)),
                                    accountingAdjustment = Some(BigDecimal(40.00)),
                                    averagingAdjustment = Some(BigDecimal(50.00)),
                                    lossBroughtForward = Some(BigDecimal(60.00)),
                                    outstandingBusinessIncome = Some(BigDecimal(70.00)))

      val updatedSource = source.copy(
        commencementDate = source.commencementDate.minusMonths(1),
        allowances = Some(allowances),
        adjustments = Some(adjustments)
      )

      verifyUpdate(source, updatedSource)
    }

    "set allowances to None if not provided" in {
      val source = selfEmployment()

      val updatedSource = source.copy(
        allowances = None
      )

      verifyUpdate(source, updatedSource)
    }

    "set each allowance to None if not provided" in {
      val source = selfEmployment()

      val updatedSource = source.copy(
        allowances = Some(Allowances())
      )

      verifyUpdate(source, updatedSource)
    }

    "set adjustments to None if not provided" in {
      val source = selfEmployment()

      val updatedSource = source.copy(
        adjustments = None
      )

      verifyUpdate(source, updatedSource)
    }

    "set each adjustment to None if not provided" in {
      val source = selfEmployment()

      val updatedSource = source.copy(
        adjustments = Some(Adjustments())
      )

      verifyUpdate(source, updatedSource)
    }

    "return false when the self employment does not exist" in {
      val result = await(selfEmploymentRepository.update(saUtr, taxYear, UUID.randomUUID().toString, selfEmployment()))
      result shouldEqual false
    }

    "not remove incomes" in {
      val source = SelfEmployment
        .create(saUtr, taxYear, selfEmployment())
        .copy(incomes = Seq(SelfEmploymentIncomeSummary(BSONObjectID.generate.stringify, IncomeType.Turnover, 10)))
      await(mongoRepository.insert(source))
      val found = await(mongoRepository.findById(saUtr, taxYear, source.sourceId)).get
      await(selfEmploymentRepository.update(saUtr, taxYear, source.sourceId, found))

      val found1 = await(mongoRepository.findById(source.id))

      found1.get.incomes should not be empty
    }

    "update last modified" in {
      val source = selfEmployment()
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, source))
      val found = await(mongoRepository.findById(BSONObjectID(sourceId)))
      await(selfEmploymentRepository.update(saUtr, taxYear, sourceId, source))

      val found1 = await(mongoRepository.findById(BSONObjectID(sourceId)))

      // Added the equals clauses as it was failing locally once, can fail if the test runs faster and has the same time for create and update
      found1.get.lastModifiedDateTime.isEqual(found.get.lastModifiedDateTime) || found1.get.lastModifiedDateTime
        .isAfter(found.get.lastModifiedDateTime) shouldBe true
    }
  }

  def cast[A](a: Any): A = a.asInstanceOf[A]

  "create summary" should {
    "add a summary to an empty list when source exists and return id" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary)))

        summaryId.isDefined shouldEqual true
        val dbSummaries = await(repo.list(saUtr, taxYear, sourceId))

        val found = dbSummaries.get
        found.headOption shouldEqual Some(summaryItem.example(id = summaryId))
      }
    }

    "add a summary to the existing list when source exists and return id" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
        val summary = summaryItem.example()
        val summary1 = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary)))
        val summaryId1 = await(repo.create(saUtr, taxYear, sourceId, cast(summary1)))

        val summaries = await(repo.list(saUtr, taxYear, sourceId))

        val found = summaries.get
        found should contain theSameElementsAs Seq(summaryItem.example(id = summaryId),
                                                   summaryItem.example(id = summaryId1))
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
        val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
        await(repo.findById(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }

    "return the summary if found" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary))).get
        val found = await(repo.findById(saUtr, taxYear, sourceId, summaryId))

        found shouldEqual Some(summaryItem.example(id = Some(summaryId)))
      }
    }
  }

  "list summaries" should {
    "return empty list when source has no summaries" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
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
        val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(saUtr, taxYear, sourceId, cast(summary))).get
        await(repo.delete(saUtr, taxYear, sourceId, summaryId)) shouldEqual true
      }
    }

    "only delete the specified summary" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
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
        val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
        await(repo.delete(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual false
      }
    }
  }

  "update income" should {
    "return true when the income has been updated" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
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
        val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
        val summary1 = summaryItem.example()
        val summaryId1 = await(repo.create(saUtr, taxYear, sourceId, cast(summary1))).get
        val summary2 = summaryItem.example()
        val summaryId2 = await(repo.create(saUtr, taxYear, sourceId, cast(summary2))).get

        val summaryToUpdate = summaryItem.example()
        await(repo.update(saUtr, taxYear, sourceId, summaryId2, cast(summaryToUpdate))) shouldEqual true

        val found = await(repo.list(saUtr, taxYear, sourceId)).get

        found should contain theSameElementsAs Seq(summaryItem.example(id = Some(summaryId1)),
                                                   summaryItem.example(id = Some(summaryId2)))
      }
    }

    "return false when the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(
          repo.update(saUtr,
                      taxYear,
                      BSONObjectID.generate.stringify,
                      BSONObjectID.generate.stringify,
                      cast(summaryItem.example()))) shouldEqual false
      }
    }

    "return false when the income does not exist" in {
      val sourceId = await(selfEmploymentRepository.create(saUtr, taxYear, selfEmployment()))
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.update(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify, cast(summaryItem.example()))) shouldEqual false
      }
    }
  }

}
