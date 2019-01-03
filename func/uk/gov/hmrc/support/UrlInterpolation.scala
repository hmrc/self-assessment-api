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

package uk.gov.hmrc.support

import scala.collection.mutable
import scala.util.matching.Regex

trait UrlInterpolation {

  def interpolated(path: Regex)(implicit urlPathVariables: mutable.Map[String, String]): String = {
    interpolated(path.regex)
  }

  def interpolated(path: String)(implicit urlPathVariables: mutable.Map[String, String]): String = {
    interpolate(interpolate(path, "sourceLocation"), "periodLocation")
  }

  private def interpolate(path: String, pathVariable: String)(implicit pathVariablesValues: mutable.Map[String, String]): String = {
    pathVariablesValues.get(pathVariable) match {
      case Some(variableValue) => path.replace(s"%$pathVariable%", variableValue)
      case None => path
    }
  }
}
