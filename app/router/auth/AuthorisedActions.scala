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

package router.auth

import play.api.Logger
import play.api.mvc.{ActionBuilder, Request, Result}
import router.errors.ErrorCode
import uk.gov.hmrc.auth.core.{AuthorisationException, AuthorisedFunctions, InvalidBearerToken}
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

trait AuthorisedActions extends AuthorisedFunctions { _: BaseController =>

  def AuthAction: ActionBuilder[Request] = new ActionBuilder[Request] {
    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
      implicit val req: Request[A] = request
      authorised()(block(request)).recover{
        case _: InvalidBearerToken =>
          Logger.warn(s"[AuthorisedActions] invalid bearer token when trying to access ${request.uri}")
          Forbidden(ErrorCode.invalidBearerToken.asJson)
        case ex: AuthorisationException =>
          Logger.warn(s"[AuthorisedActions] authorisation exception caught when trying to access ${request.uri} : ${ex.reason}")
          Forbidden(ErrorCode.unauthorisedError.asJson)
      }
    }
  }
}
