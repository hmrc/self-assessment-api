/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json._
import play.api.libs.streams.Accumulator
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.resources.models.SourceType.SourceType

import scala.concurrent.Future

class FeatureSwitchAction(source: SourceType, summary: String) extends ActionBuilder[Request] {
  private val isFeatureEnabled = FeatureSwitch(AppContext.featureSwitch).isEnabled(source, summary)
  private val notFound = Future.successful(NotFound)

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    block(request)
  }

  def asyncFeatureSwitch(block: Request[JsValue] => Future[Result]) = {

    val emptyJsonParser: BodyParser[JsValue] = BodyParser { request => Accumulator.done(Right(JsNull)) }

    if (isFeatureEnabled) async(BodyParsers.parse.json)(block)
    else async[JsValue](emptyJsonParser)(_ => notFound)
  }

  def asyncFeatureSwitch(block: => Future[Result]) = {
    if (isFeatureEnabled) async(block)
    else async(notFound)
  }

}

object FeatureSwitchAction {
  def apply(source: SourceType, summary: String = "") = new FeatureSwitchAction(source, summary)
}
