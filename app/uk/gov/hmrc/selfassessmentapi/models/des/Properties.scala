/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.des

import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.{Mapper, properties}

case class Properties()

object Properties {
  implicit val reads: Reads[Properties] = new Reads[Properties] {
    override def reads(json: JsValue) = json match {
      case JsObject(_) => JsSuccess(Properties())
      case _ => JsError()
    }
  }
  implicit val writes: Writes[Properties] = new Writes[Properties] {
    override def writes(o: Properties) = Json.obj()
  }

  implicit object MapperInstance extends Mapper[properties.Properties, Properties] {
    override def from(apiProperties: properties.Properties): Properties = Properties()
  }

}
