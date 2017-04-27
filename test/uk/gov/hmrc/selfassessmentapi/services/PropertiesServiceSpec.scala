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

package uk.gov.hmrc.selfassessmentapi.services

import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models.properties.Properties

class PropertiesServiceSpec extends MongoEmbeddedDatabase {

  private val service = new PropertiesService(new PropertiesRepository)
  private val nino = generateNino

  "create" should {
    "return an error when a customer attempts to create more than one property business" in {
      val properties = Properties()

      await(service.create(nino, properties)) shouldBe Right(true)
      await(service.create(nino, properties)) shouldBe Left(Error("ALREADY_EXISTS", "A property business already exists", Some("")))
    }
  }
}
