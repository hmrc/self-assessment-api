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

package router.services

import play.api.mvc._
import router.constants.Versions
import router.errors.{IncorrectAPIVersion, UnsupportedAPIVersion}
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import utils.Logging

import scala.concurrent.Future

trait Service extends Logging {

  private[services] def withApiVersion[A](pf: PartialFunction[Option[String], Future[SelfAssessmentOutcome]])
                       (implicit request: Request[A]): Future[SelfAssessmentOutcome] = {
    pf.orElse[Option[String], Future[SelfAssessmentOutcome]] {
      case Some(_) =>
        logger.info("request header contains an unsupported api version")
        Future.successful(Left(UnsupportedAPIVersion))
      case None =>
        logger.info("request header contains an incorrect or empty api version")
        Future.successful(Left(IncorrectAPIVersion))
    }(Versions.extractAcceptHeader(request).map(_.version))
  }
}
