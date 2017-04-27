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

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.domain.Properties
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertiesService(repository: PropertiesRepository) {

  def retrieve(nino: Nino): Future[Option[properties.Properties]] = {
    repository.retrieve(nino).map {
      case Some(properties) => Some(properties.toModel)
      case None => None
    }
  }

  def create(nino: Nino, props: properties.Properties): Future[Either[Error, Boolean]] = {
    val properties = Properties(BSONObjectID.generate, nino)

    repository.retrieve(nino) flatMap {
      case Some(_) => Future.successful(Left(Error(ErrorCode.ALREADY_EXISTS.toString, s"A property business already exists", Some(""))))
      case None => repository.create(properties).map(Right(_))
    }
  }

}

object PropertiesService {
  def apply(): PropertiesService = new PropertiesService(PropertiesRepository())
}
