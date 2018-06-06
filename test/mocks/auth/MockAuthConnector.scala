/*
 * Copyright 2018 HM Revenue & Customs
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

package mocks.auth

import mocks.Mock
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, Retrieval}

import scala.concurrent.Future

trait MockAuthConnector extends Mock { _: Suite =>

  val mockAuthConnector = mock[AuthConnector]

  object MockAuthConnector {
    def authorise[T](predicate: Predicate, retrieval: Retrieval[T]): OngoingStubbing[Future[T]] = {
      when(mockAuthConnector.authorise[T](eqTo(predicate), eqTo(retrieval))(any(), any()))
    }

    def authorise(): OngoingStubbing[Future[Unit]] = authorise(EmptyPredicate, EmptyRetrieval)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }
}
