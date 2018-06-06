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

package router.definition

import play.api.libs.json.{Format, Json}
import router.definition.APIStatus.APIStatus
import router.enums.EnumJson


case class Access(`type`: String, whitelistedApplicationIds: Seq[String])

object Access {
  implicit val formatAccess = Json.format[Access]
}

case class Parameter(name: String, required: Boolean = false)

object Parameter {
  implicit val formatParameter = Json.format[Parameter]
}

case class PublishingException(message: String) extends Exception(message)

object APIStatus extends Enumeration {
  type APIStatus = Value
  val ALPHA, BETA, STABLE, DEPRECATED, RETIRED = Value

  implicit val formatAPIStatus: Format[APIStatus] = EnumJson.enumFormat(APIStatus)
}

case class APIVersion(version: String,
                      access: Option[Access] = None,
                      status: APIStatus,
                      endpointsEnabled: Boolean) {

  require(version.nonEmpty, "version is required")
}

object APIVersion {
  implicit val formatAPIVersion = Json.format[APIVersion]
}

case class APIDefinition(name: String,
                         description: String,
                         context: String,
                         versions: Seq[APIVersion],
                         requiresTrust: Option[Boolean]) {

  require(name.nonEmpty, "name is required")
  require(context.nonEmpty, "context is required")
  require(description.nonEmpty, "description is required")
  require(versions.nonEmpty, "at least one version is required")
  require(uniqueVersions, "version numbers must be unique")

  private def uniqueVersions = {
    !versions.map(_.version).groupBy(identity).mapValues(_.size).exists(_._2 > 1)
  }
}

object APIDefinition {
  implicit val formatAPIDefinition = Json.format[APIDefinition]
}

case class Scope(key: String,
                 name: String,
                 description: String)

object Scope {
  implicit val formatScope = Json.format[Scope]
}

case class Definition(scopes: Seq[Scope],
                      api: APIDefinition)

object Definition {
  implicit val formatDefinition = Json.format[Definition]
}