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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.connectors.ObligationsConnector
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.resources.Audit.makeObligationsRetrievalAudit
import uk.gov.hmrc.selfassessmentapi.services.AuditService.audit

import scala.concurrent.ExecutionContext.Implicits.global

object SelfEmploymentObligationsResource extends BaseResource {
  private val connector = ObligationsConnector

  def retrieveObligations(nino: Nino, id: SourceId): Action[Unit] =
    APIAction(nino, SourceType.SelfEmployments, Some("obligations")).async(parse.empty) { implicit request =>
      connector.get(nino).map { response =>
        audit(
          makeObligationsRetrievalAudit(nino, Some(id), request.authContext, response, SelfEmploymentRetrieveObligations))
        response.filter {
          case 200 =>
            logger.debug("Self-employment obligations from DES = " + Json.stringify(response.json))
            response.obligations("ITSB", Some(id)) match {
              case Right(obj) => obj.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
              case Left(ex) =>
                logger.error(ex.msg)
                InternalServerError(Json.toJson(Errors.InternalServerError))
            }
        }
      }
    }
}
