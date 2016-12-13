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

import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.api.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.repositories.domain.Liability

import scala.concurrent.ExecutionContext.Implicits.global

class LiabilityRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val repository = new LiabilityMongoRepository()
  private val nino = NinoGenerator().nextNino()

  override def beforeEach() {
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  "save" should {

    "create new liability if there is no liability for given utr and tax year" in {
      val liability = Liability.create(nino, taxYear, SelfAssessment())
      await(repository.save(liability))

      await(repository.findAll()) shouldBe List(liability)
    }

    "replace current liability if there is liability for given utr and tax year" in {
      val liability = Liability.create(nino, taxYear, SelfAssessment())
      await(repository.save(liability))

      val updatedLiability = liability.copy(totalIncomeReceived = 100)
      await(repository.save(updatedLiability))

      await(repository.findAll()) shouldBe List(updatedLiability)
    }

    "not replace liability for a different utr and tax year" in {
      val liability = Liability.create(NinoGenerator().nextNino(), taxYear, SelfAssessment())
      await(repository.save(liability))

      val anotherLiability = Liability.create(NinoGenerator().nextNino(), taxYear, SelfAssessment())
      await(repository.save(anotherLiability))

      await(repository.findAll()) shouldBe List(liability, anotherLiability)
    }
  }

  "findBy" should {

    "return liability for given utr and tax year" in {

      val liability = Liability.create(nino, taxYear, SelfAssessment())
      await(repository.save(liability))

      await(repository.findBy(nino, taxYear)) shouldBe Some(liability)
    }

    "return None if there is no liability for given utr and tax year" in {
      val anotherLiability = Liability.create(NinoGenerator().nextNino(), taxYear, SelfAssessment())
      await(repository.save(anotherLiability))

      await(repository.findBy(nino, taxYear)) shouldBe None
    }
  }
}
