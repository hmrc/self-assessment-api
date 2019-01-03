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

package config.locator

import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.api.domain.Registration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ServiceLocatorConnector @Inject()(config: AppConfig,
                                        http: HttpClient) {

  val metadata: Option[Map[String, String]] = Some(Map("third-party-api" -> "true"))

  def register(implicit hc: HeaderCarrier): Future[Boolean] = {
    val registration = Registration(config.appNameForServiceLocator, config.appUrl, metadata)
    http.POST(s"${config.serviceLocatorUrl}/registration", registration, Seq("Content-Type" -> "application/json")) map {
      _ =>
        Logger.info("Service is registered on the service locator")
        true
    } recover {
      case e: Throwable =>
        Logger.error(s"Service could not register on the service locator", e)
        false
    }
  }
}
