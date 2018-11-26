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

package uk.gov.hmrc.r2.selfassessmentapi.models.properties

import play.api.libs.json.Format
import uk.gov.hmrc.r2.selfassessmentapi.models.EnumJson

object PropertyType extends Enumeration {
  type PropertyType = Value

  val FHL = Value("furnished-holiday-lettings")
  val OTHER = Value("other")

  implicit val format: Format[PropertyType] =
    EnumJson.enumFormat(PropertyType,
      Some(s"Property business type should be one of: ${PropertyType.values.mkString(", ")}"))
}
