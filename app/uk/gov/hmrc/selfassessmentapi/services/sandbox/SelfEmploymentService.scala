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

package uk.gov.hmrc.selfassessmentapi.services.sandbox

import org.joda.time.LocalDate
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.domain.{SelfEmployment, SelfEmploymentId}

import scala.concurrent.Future

object SelfEmploymentService extends uk.gov.hmrc.selfassessmentapi.services.SelfEmploymentService {

  override def create(selfEmployment: SelfEmployment): Future[SelfEmploymentId] = Future.successful(BSONObjectID.generate.stringify)

  override def findBySelfEmploymentId(utr: SaUtr, selfEmploymentId: SelfEmploymentId): Future[Option[SelfEmployment]] =
    Future.successful(Some(SelfEmployment(Some(selfEmploymentId), "Awesome Bakers", LocalDate.now)))

  override def update(selfEmployment: SelfEmployment, utr: SaUtr, selfEmploymentId: SelfEmploymentId): Future[Unit] =
    Future.successful(())

  override def delete(utr: SaUtr, selfEmploymentId: SelfEmploymentId): Future[Boolean] =
    Future.successful(true)
}
