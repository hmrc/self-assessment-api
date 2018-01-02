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

package uk.gov.hmrc.selfassessmentapi.services

import play.api.Logger
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.connectors.BusinessDetailsConnector
import uk.gov.hmrc.selfassessmentapi.models.MtdId
import uk.gov.hmrc.selfassessmentapi.repositories.MtdReferenceRepository

import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.{ExecutionContext, Future}

trait MtdRefLookupService {
  private val logger = Logger(MtdRefLookupService.getClass)
  val businessConnector: BusinessDetailsConnector
  val repository: MtdReferenceRepository

  def mtdReferenceFor(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Int, MtdId]] = {
    repository.retrieve(nino).flatMap {
      case Some(mtdId) =>
        logger.debug("NINO to MTD Ref lookup cache hit.")
        Future.successful(Right(mtdId))
      case None =>
        logger.debug("NINO to MTD Ref lookup cache miss.")
        cacheReferenceFor(nino)
    }
  }

  private def cacheReferenceFor(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Int, MtdId]] = {
    businessConnector.get(nino).map { response =>
      response.status match {
        case 200 =>
          logger.debug(s"NINO to MTD reference lookup successful. Status code: [${response.status}]")
          response.mtdId match {
            case Some(id) =>
              repository.store(nino, id)
              Right(id)
            case None =>
              Left(500)
          }
        case 400 =>
          logger.debug(s"NINO to MTD reference lookup was invalid. Status code: [${response.status}]")
          Left(400)
        case 404 =>
          logger.debug(s"NINO to MTD reference lookup was not found. Status code: [${response.status}]")
          Left(403)
        case 500 =>
          logger.error(s"NINO to MTD reference lookup failed with server error. Is there an issue with DES? Status code: [${response.status}]")
          Left(500)
        case 503 | _ =>
          logger.error(s"NINO to MTD reference lookup failed with service error. Is there an issue with DES? Status code: [${response.status}]")
          Left(500)
      }
    }
  }
}

object MtdRefLookupService extends MtdRefLookupService {
  override val businessConnector: BusinessDetailsConnector = BusinessDetailsConnector
  override lazy val repository: MtdReferenceRepository = MtdReferenceRepository()
}
