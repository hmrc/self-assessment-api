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

import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.models.MtdId

import scala.concurrent.ExecutionContext.Implicits.global

class MtdReferenceRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {
  private lazy val repo = new MtdReferenceRepository()(mongo)

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.ensureIndexes
  }

  "create" should {
    "return true and insert a record containing a NINO and its associated MTD reference number" in {
      val nino = generateNino
      val mtdRef = MtdId("123")

      await(repo.store(nino, mtdRef)) shouldBe true

      val result = await(repo.retrieve(nino))
      result shouldBe Some(mtdRef)
    }

    "return false if an attempt is made to insert the same nino twice" in {
      val nino = Nino("AA999999A")
      val mtdRef = MtdId("123")

      await(repo.store(Nino("BB999999B"), mtdRef)) shouldBe true
      await(repo.store(nino, mtdRef)) shouldBe true
      await(repo.store(nino, mtdRef)) shouldBe false
    }
  }

  "retrieve" should {
    "return None if the NINO does not exist in the database" in {
      await(repo.retrieve(generateNino)) shouldBe None
    }
  }
}
