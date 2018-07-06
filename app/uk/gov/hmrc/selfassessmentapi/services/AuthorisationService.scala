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
import play.api.libs.json.Json.toJson
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.selfassessmentapi.config.MicroserviceAuthConnector
import uk.gov.hmrc.selfassessmentapi.contexts.{Agent, AuthContext, FilingOnlyAgent, Individual}
import uk.gov.hmrc.selfassessmentapi.models.Errors.{ClientNotSubscribed, NinoInvalid}
import uk.gov.hmrc.selfassessmentapi.models.{Errors, MtdId}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.matching.Regex

object AuthorisationService extends AuthorisedFunctions {
  type AuthResult = Either[Result, AuthContext]

  private val lookupService = MtdRefLookupService

  override def authConnector: AuthConnector = MicroserviceAuthConnector

  private val logger = Logger(AuthorisationService.getClass)

  def authCheck(nino: Nino)(implicit hc: HeaderCarrier, reqHeader: RequestHeader, ec: ExecutionContext): Future[AuthResult] =
    lookupService.mtdReferenceFor(nino).flatMap {
      case Right(id) => authoriseAsClient(id)
      case Left(status) =>
        status match {
          case 400 => Future.successful(Left(BadRequest(toJson(NinoInvalid))))
          case 403 => Future.successful(Left(Forbidden(toJson(ClientNotSubscribed))))
          case 500 => Future.successful(Left(InternalServerError(toJson(Errors.InternalServerError))))
        }
    }

  def getAgentReference(enrolments: Enrolments): Option[String] =
    enrolments.enrolments
      .flatMap(_.identifiers)
      .find(_.key == "AgentReferenceNumber")
      .map(_.value)

  private def authoriseAsClient(mtdId: MtdId)(implicit hc: HeaderCarrier,
                                              requestHeader: RequestHeader,
                                              ec: ExecutionContext): Future[AuthResult] = {
    logger.debug("Attempting to authorise user as a fully-authorised individual.")
    authorised(
      Enrolment("HMRC-MTD-IT")
        .withIdentifier("MTDITID", mtdId.mtdId)
        .withDelegatedAuthRule("mtd-it-auth"))
      .retrieve(Retrievals.affinityGroup and Retrievals.agentCode and Retrievals.authorisedEnrolments) {
        case Some(AffinityGroup.Agent) ~ Some(agentCode) ~ enrolments =>
          logger.debug("Client authorisation succeeded as fully-authorised agent.")
          Future.successful(Right(Agent(agentCode = Some(agentCode), agentReference = getAgentReference(enrolments))))
        case Some(AffinityGroup.Agent) ~ None ~ enrolments =>
          logger.debug("Client authorisation succeeded as fully-authorised agent but could not retrieve agentCode.")
          Future.successful(Right(Agent(agentCode = None, agentReference = getAgentReference(enrolments))))
        case _ =>
          logger.debug("Client authorisation succeeded as fully-authorised individual.")
          Future.successful(Right(Individual))
      } recoverWith (authoriseAsFOA orElse unhandledError)
  }

  private def authoriseAsFOA(implicit hc: HeaderCarrier,
                             reqHeader: RequestHeader,
                             ec: ExecutionContext): PartialFunction[Throwable, Future[AuthResult]] = {
    case _: InsufficientEnrolments =>
      authorised(AffinityGroup.Agent and Enrolment("HMRC-AS-AGENT"))
        .retrieve(Retrievals.agentCode and Retrievals.authorisedEnrolments) { // If the user is an agent are they enrolled in Agent Services?
          case optAgentCode ~ enrolments =>
            if (reqHeader.method == "GET") {
              logger.debug("Client authorisation failed. Attempt to GET as a filing-only agent.")
              Future.successful(Left(Forbidden(toJson(Errors.AgentNotAuthorized))))
            } else
              optAgentCode match {
                case Some(agentCode) =>
                  logger.debug("Client authorisation succeeded as filing-only agent.")
                  Future.successful(
                    Right(FilingOnlyAgent(agentCode = Some(agentCode), agentReference = getAgentReference(enrolments))))
                case None =>
                  logger.debug("Agent code was not returned by auth for agent user")
                  Future.successful(
                    Right(FilingOnlyAgent(agentCode = None, agentReference = getAgentReference(enrolments))))

              }
        } recoverWith (unsubscribedAgentOrUnauthorisedClient orElse unhandledError) // Iff agent is not enrolled for the user or client affinityGroup is not Agent.
  }

  private def unsubscribedAgentOrUnauthorisedClient: PartialFunction[Throwable, Future[AuthResult]] = {
    case _: InsufficientEnrolments =>
      logger.debug(s"Authorisation failed as filing-only agent.")
      Future.successful(Left(Forbidden(toJson(Errors.AgentNotSubscribed))))
    case _: UnsupportedAffinityGroup =>
      logger.debug(s"Authorisation failed as client.")
      Future.successful(Left(Forbidden(toJson(ClientNotSubscribed))))
  }

  private def unhandledError: PartialFunction[Throwable, Future[AuthResult]] = {
    val regex: Regex = """.*"Unable to decrypt value".*""".r
    lazy val internalServerError = Future.successful(
      Left(InternalServerError(toJson(Errors.InternalServerError("An internal server error occurred")))))

    locally { // http://www.scala-lang.org/old/node/3594
      case e @ (_: AuthorisationException | Upstream5xxResponse(regex(_*), _, _)) =>
        logger.error(s"Authorisation failed with unexpected exception. Bad token? Exception: [$e]")
        Future.successful(Left(Forbidden(toJson(Errors.BadToken))))
      case e: Upstream4xxResponse =>
        logger.error(s"Unhandled 4xx response from play-auth: [$e]. Returning 500 to client.")
        internalServerError
      case e: Upstream5xxResponse =>
        logger.error(s"Unhandled 5xx response from play-auth: [$e]. Returning 500 to client.")
        internalServerError
      case NonFatal(e) =>
        logger.error(s"Unhandled non-fatal exception from play-auth: [$e]. Returning 500 to client.")
        internalServerError
    }
  }
}
