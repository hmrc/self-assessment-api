/*
 * Copyright 2019 HM Revenue & Customs
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

import org.joda.time.{DateTime, DateTimeUtils, DateTimeZone}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.MongoConnector
import uk.gov.hmrc.selfassessmentapi.domain.Dividends
import uk.gov.hmrc.selfassessmentapi.mocks.Mock
import uk.gov.hmrc.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.selfassessmentapi.{MongoEmbeddedDatabase, NinoGenerator, models}

import scala.concurrent.ExecutionContext.Implicits.global

class DividendsRepositorySpec extends MongoEmbeddedDatabase with Mock {

  val mockReactiveMongoComponent = mock[ReactiveMongoComponent]
  val mockMongoConnector = mock[MongoConnector]
  when(mockReactiveMongoComponent.mongoConnector).thenReturn(mockMongoConnector)
  when(mockMongoConnector.db).thenReturn(mongo)

  private lazy val repo = new DividendsRepository(mockReactiveMongoComponent)

  private val nino = NinoGenerator().nextNino()

  def createDividend(nino: Nino, id: BSONObjectID = BSONObjectID.generate): Dividends = {
    Dividends(BSONObjectID.generate, nino, Map(TaxYear("2017-18") -> models.dividends.Dividends(Some(500.25))))
  }

  "create" should {
    "store a dividends object" in {
      await(repo.create(createDividend(nino))) shouldBe true

      val result = await(repo.retrieve(nino))
      result.isDefined shouldBe true
      result.get.dividends.get(TaxYear("2017-18")) shouldBe Some(models.dividends.Dividends(Some(500.25)))
    }
  }

  "update" should {
    "overwrite an existing dividend" in {
      val originalDividend = createDividend(nino)
      val updatedDividend = originalDividend.copy(dividends =
        originalDividend.dividends.updated(TaxYear("2017-18"), models.dividends.Dividends(None)))

      await(repo.create(originalDividend)) shouldBe true
      await(repo.update(nino, updatedDividend)) shouldBe true

      val result = await(repo.retrieve(nino))
      result.isDefined shouldBe true
      result.get.dividends.get(TaxYear("2017-18")) shouldBe Some(models.dividends.Dividends(None))
    }

    "update the lastModifiedDateTime on the persisted object" in {
      val dividend = createDividend(nino)
      await(repo.create(dividend))

      val creationDateTime = await(repo.retrieve(nino)).get.lastModifiedDateTime

      DateTimeUtils.setCurrentMillisFixed(DateTime.now().plusDays(1).getMillis)

      await(repo.update(nino, dividend.copy(dividends =
        dividend.dividends.updated(TaxYear("2017-18"), models.dividends.Dividends(None)))))

      val lastModifiedDateTime = await(repo.retrieve(nino)).get.lastModifiedDateTime

      lastModifiedDateTime.isAfter(creationDateTime) shouldBe true

      DateTimeUtils.setCurrentMillisSystem()
    }
  }

  "retrieve" should {
    "return None if the user has no dividends" in {
      await(repo.retrieve(nino)) shouldBe None
    }
  }

  "deleteAllBeforeDate" should {
    "delete all records older than the provided DateTime object" in {
      val dividendToKeepOne = createDividend(NinoGenerator().nextNino()).copy(lastModifiedDateTime = DateTime.now(DateTimeZone.UTC).plusDays(1))
      val dividendToKeepTwo = createDividend(nino)
      val dividendToRemoveOne = createDividend(NinoGenerator().nextNino()).copy(lastModifiedDateTime = DateTime.now(DateTimeZone.UTC).minusDays(1))

      await(repo.create(dividendToKeepOne))
      await(repo.create(dividendToRemoveOne))
      await(repo.create(dividendToKeepTwo))
      await(repo.deleteAllBeforeDate(DateTime.now(DateTimeZone.UTC).minusHours(1))) shouldBe 1
      await(repo.findAll()) should contain theSameElementsAs Seq(dividendToKeepOne, dividendToKeepTwo)
    }
  }
}
