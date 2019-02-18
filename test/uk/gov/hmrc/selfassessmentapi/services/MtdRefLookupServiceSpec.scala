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

package uk.gov.hmrc.selfassessmentapi.services

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.connectors.BusinessDetailsConnector
import uk.gov.hmrc.selfassessmentapi.models.MtdId
import uk.gov.hmrc.selfassessmentapi.repositories.MtdReferenceRepository
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.BusinessDetailsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MtdRefLookupServiceSpec extends UnitSpec with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val businessConnector = mock[BusinessDetailsConnector]
  val repository = mock[MtdReferenceRepository]


  "mtdReferenceFor" should {
    "skip the caching process if the NINO to MTD reference mapping exists in the cache" in {
      val nino = generateNino
      val unitUnderTest = new MtdRefLookupService(businessConnector, repository)

      when(repository.retrieve(nino)).thenReturn(Future.successful(Some(MtdId("abc"))))

      await(unitUnderTest.mtdReferenceFor(nino)) shouldBe Right(MtdId("abc"))
      verify(repository, times(1)).retrieve(nino)
      verifyNoMoreInteractions(repository)
      verifyZeroInteractions(businessConnector)
    }

    "retrieve and cache the NINO to MTD reference mapping if it does not already exist, and the user is subscribed" in {
      val nino = generateNino
      val mtdId = MtdId("abc")
      val unitUnderTest = new MtdRefLookupService(businessConnector, repository)
      val mockResponse = mock[BusinessDetailsResponse]

      when(mockResponse.status).thenReturn(200)
      when(mockResponse.mtdId).thenReturn(Some(mtdId))
      when(repository.retrieve(nino)).thenReturn(Future.successful(None))
      when(repository.store(nino, mtdId)).thenReturn(Future.successful(true))
      when(businessConnector.get(nino)).thenReturn(Future.successful(mockResponse))

      await(unitUnderTest.mtdReferenceFor(nino)) shouldBe Right(mtdId)
      verify(businessConnector, times(1)).get(nino)
      verify(repository, times(1)).retrieve(nino)
      verify(repository, times(1)).store(nino, mtdId)
    }

    "return None if the NINO to MTD reference mapping does not already exist, and the user is not subscribed" in {
      val nino = generateNino
      val unitUnderTest = new MtdRefLookupService(businessConnector, repository)
      val mockResponse = mock[BusinessDetailsResponse]

      when(mockResponse.status).thenReturn(404)
      when(repository.retrieve(nino)).thenReturn(Future.successful(None))
      when(businessConnector.get(nino)).thenReturn(Future.successful(mockResponse))

      await(unitUnderTest.mtdReferenceFor(nino)) shouldBe Left(403)
      verify(businessConnector, times(1)).get(nino)
      verify(repository, times(1)).retrieve(nino)
      verifyNoMoreInteractions(repository)
    }

    "return None if the NINO to MTD reference mapping does not already exist, and DES is experiencing issues" in {
      val nino = generateNino
      val unitUnderTest = new MtdRefLookupService(businessConnector, repository)
      val mockResponse = mock[BusinessDetailsResponse]

      when(mockResponse.status).thenReturn(500)
      when(repository.retrieve(nino)).thenReturn(Future.successful(None))
      when(businessConnector.get(nino)).thenReturn(Future.successful(mockResponse))

      await(unitUnderTest.mtdReferenceFor(nino)) shouldBe Left(500)
      verify(businessConnector, times(1)).get(nino)
      verify(repository, times(1)).retrieve(nino)
      verifyNoMoreInteractions(repository)
    }
  }

}
