/*
 * Copyright 2020 HM Revenue & Customs
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

package config

import javax.inject.Inject
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.{Configuration, Logger}
import router.constants.Versions
import router.constants.Versions.VERSION_2
import router.errors.ErrorCode
import router.errors.ErrorCode._
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.http.JsonErrorHandler

import scala.concurrent.{ExecutionContext, Future}

class ErrorHandler @Inject()(
                              config: Configuration,
                              auditConnector: AuditConnector
                            )
                            (implicit ec: ExecutionContext) extends JsonErrorHandler(config, auditConnector) {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {

    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    getAPIVersionFromRequest match {
      case Some(VERSION_2) =>
        Logger.warn(s"[ErrorHandler][onClientError] error in version 2, for (${request.method}) [${request.uri}] with status: $statusCode and message: $message")
        statusCode match {
          case BAD_REQUEST =>
            auditConnector.sendEvent(dataEvent("ServerValidationError", "Request bad format exception", request))
            Future.successful(BadRequest(Json.toJson(invalidRequest)))
          case NOT_FOUND =>
            auditConnector.sendEvent(dataEvent("ResourceNotFound", "Resource Endpoint Not Found", request))
            Future.successful(NotFound(Json.toJson(matchingResourceNotFound)))
          case _ =>
            val errorCode = statusCode match {
              case UNAUTHORIZED => unauthorisedError
              case UNSUPPORTED_MEDIA_TYPE => invalidBodyType
              case _ => ErrorCode("INVALID_REQUEST", message)
            }

            auditConnector.sendEvent(
              dataEvent(
                eventType = "ClientError",
                transactionName = s"A client error occurred, status: $statusCode",
                request = request,
                detail = Map.empty
              )
            )

            Future.successful(Status(statusCode)(Json.toJson(errorCode)))
        }
      case _ =>
        Logger.warn(s"[ErrorHandler][onClientError], error for (${request.method}) [${request.uri}] with status: $statusCode and message: $message")
        ErrorHandler.super.onClientError(request, statusCode, message)
    }
  }

  override def onServerError(request: RequestHeader, ex: Throwable): Future[Result] = {
    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    getAPIVersionFromRequest match {
      case Some(VERSION_2) =>
        Logger.warn(s"[ErrorHandler][onServerError] Internal server error in version 2, for (${request.method}) [${request.uri}] -> ", ex)

        val (status, errorCode, eventType) = ex match {
          case _: NotFoundException => (NOT_FOUND, matchingResourceNotFound, "ResourceNotFound")
          case _: AuthorisationException => (UNAUTHORIZED, unauthorisedError, "ClientError")
          case _: JsValidationException => (BAD_REQUEST, invalidRequest, "ServerValidationError")
          case e: HttpException => (e.responseCode, invalidRequest, "ServerValidationError")
          case e: Upstream4xxResponse => (e.reportAs, invalidRequest, "ServerValidationError")
          case e: Upstream5xxResponse => (e.reportAs, internalServerError, "ServerInternalError")
          case _ => (INTERNAL_SERVER_ERROR, internalServerError, "ServerInternalError")
        }

        auditConnector.sendEvent(
          dataEvent(
            eventType = eventType,
            transactionName = "Unexpected error",
            request = request,
            detail = Map("transactionFailureReason" -> ex.getMessage)
          )
        )

        Future.successful(Status(status)(Json.toJson(errorCode)))

      case _ => ErrorHandler.super.onServerError(request, ex)
    }
  }

  private def getAPIVersionFromRequest(implicit hc: HeaderCarrier): Option[String] =
    Versions.getFromRequest
}
