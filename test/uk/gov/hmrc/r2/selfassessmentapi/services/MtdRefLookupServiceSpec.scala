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

package uk.gov.hmrc.r2.selfassessmentapi.services

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.r2.selfassessmentapi.UnitSpec
import uk.gov.hmrc.r2.selfassessmentapi.connectors.BusinessDetailsConnector
import uk.gov.hmrc.r2.selfassessmentapi.models.MtdId
import uk.gov.hmrc.r2.selfassessmentapi.repositories.MtdReferenceRepository
import uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers.BusinessDetailsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MtdRefLookupServiceSpec extends UnitSpec with MockitoSugar {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "mtdReferenceFor" should {
    "skip the caching process if the NINO to MTD reference mapping exists in the cache" in {
      val nino = generateNino
      val unitUnderTest = new TestLookupService

      when(unitUnderTest.repository.retrieve(nino)).thenReturn(Future.successful(Some(MtdId("abc"))))

      await(unitUnderTest.mtdReferenceFor(nino)) shouldBe Right(MtdId("abc"))
      verify(unitUnderTest.repository, times(1)).retrieve(nino)
      verifyNoMoreInteractions(unitUnderTest.repository)
      verifyZeroInteractions(unitUnderTest.businessConnector)
    }

    "retrieve and cache the NINO to MTD reference mapping if it does not already exist, and the user is subscribed" in {
      val nino = generateNino
      val mtdId = MtdId("abc")
      val unitUnderTest = new TestLookupService
      val mockResponse = mock[BusinessDetailsResponse]

      when(mockResponse.status).thenReturn(200)
      when(mockResponse.mtdId).thenReturn(Some(mtdId))
      when(unitUnderTest.repository.retrieve(nino)).thenReturn(Future.successful(None))
      when(unitUnderTest.repository.store(nino, mtdId)).thenReturn(Future.successful(true))
      when(unitUnderTest.businessConnector.get(nino)).thenReturn(Future.successful(mockResponse))

      await(unitUnderTest.mtdReferenceFor(nino)) shouldBe Right(mtdId)
      verify(unitUnderTest.businessConnector, times(1)).get(nino)
      verify(unitUnderTest.repository, times(1)).retrieve(nino)
      verify(unitUnderTest.repository, times(1)).store(nino, mtdId)
    }

    "return None if the NINO to MTD reference mapping does not already exist, and the user is not subscribed" in {
      val nino = generateNino
      val unitUnderTest = new TestLookupService
      val mockResponse = mock[BusinessDetailsResponse]

      when(mockResponse.status).thenReturn(404)
      when(unitUnderTest.repository.retrieve(nino)).thenReturn(Future.successful(None))
      when(unitUnderTest.businessConnector.get(nino)).thenReturn(Future.successful(mockResponse))

      await(unitUnderTest.mtdReferenceFor(nino)) shouldBe Left(403)
      verify(unitUnderTest.businessConnector, times(1)).get(nino)
      verify(unitUnderTest.repository, times(1)).retrieve(nino)
      verifyNoMoreInteractions(unitUnderTest.repository)
    }

    "return None if the NINO to MTD reference mapping does not already exist, and DES is experiencing issues" in {
      val nino = generateNino
      val unitUnderTest = new TestLookupService
      val mockResponse = mock[BusinessDetailsResponse]

      when(mockResponse.status).thenReturn(500)
      when(unitUnderTest.repository.retrieve(nino)).thenReturn(Future.successful(None))
      when(unitUnderTest.businessConnector.get(nino)).thenReturn(Future.successful(mockResponse))

      await(unitUnderTest.mtdReferenceFor(nino)) shouldBe Left(500)
      verify(unitUnderTest.businessConnector, times(1)).get(nino)
      verify(unitUnderTest.repository, times(1)).retrieve(nino)
      verifyNoMoreInteractions(unitUnderTest.repository)
    }
  }

  class TestLookupService extends MtdRefLookupService {
    override val businessConnector = mock[BusinessDetailsConnector]
    override val repository = mock[MtdReferenceRepository]
  }

}
