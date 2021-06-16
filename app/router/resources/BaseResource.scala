/*
 * Copyright 2021 HM Revenue & Customs
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

package router.resources

import config.AppConfig
import play.api.libs.json.JsValue
import play.api.mvc._
import router.errors.{ErrorCode, IncorrectAPIVersion, SelfAssessmentAPIError, UnsupportedAPIVersion}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions, InvalidBearerToken}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class BaseResource @Inject()(cc: ControllerComponents, connector: AuthConnector)(implicit appConfig: AppConfig) extends BackendController(cc) with Logging {

  def AuthAction: ActionBuilder[Request, AnyContent] = new ActionBuilder[Request, AnyContent] {

    private val authFunction: AuthorisedFunctions = new AuthorisedFunctions {
      override def authConnector: AuthConnector = connector
    }

    override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

    override implicit protected def executionContext: ExecutionContext = cc.executionContext

    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
      implicit val req: Request[A] = request

      if (appConfig.deprecatedRoutes.exists(route => (req.method == route.method) && req.uri.matches(route.routeRegex))) {
        logger.warn(s"[BaseResource] tried to access deprecated route matching [method: ${req.method}] and [uri: ${req.uri}]")
        Future.successful(Gone(ErrorCode.resourceGone.asJson))
      } else {
        authFunction.authorised()(block(request)).recover {
          case _: InvalidBearerToken =>
            logger.warn(s"[AuthorisedActions] invalid bearer token when trying to access ${request.uri}")
            Forbidden(ErrorCode.invalidBearerToken.asJson)
          case ex: AuthorisationException =>
            logger.warn(s"[AuthorisedActions] authorisation exception caught when trying to access ${request.uri} : ${ex.reason}")
            Forbidden(ErrorCode.unauthorisedError.asJson)
        }
      }
    }
  }

  private[resources] def buildResponse(apiResponse: HttpResponse): Result = {
    Try(apiResponse.json) match {
      case Success(_: JsValue) =>
        new Status(apiResponse.status)(apiResponse.json)
          .withHeaders(toSimpleHeaders(apiResponse.headers): _*)
      case _                   =>
        new Status(apiResponse.status)
          .withHeaders(toSimpleHeaders(apiResponse.headers): _*)
    }
  }

  private[resources] def buildErrorResponse(error: SelfAssessmentAPIError): Result = {
    error match {
      case IncorrectAPIVersion   => NotAcceptable(ErrorCode.invalidAcceptHeader.asJson)
      case UnsupportedAPIVersion => NotFound(ErrorCode.notFound.asJson)
    }
  }

  def toSimpleHeaders(headers: Map[String, Seq[String]]): Seq[(String, String)] = {
    (headers ++ Map(
      "X-Content-Type-Options" -> Seq("nosniff"),
      "Content-Type" -> Seq("application/json")
    )).flatMap { case (name, values) => values.map(name -> _) }.toSeq
  }

}