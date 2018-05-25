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
import router.definition.AuthType.AuthType
import router.definition.GroupName.GroupName
import router.definition.HttpMethod.HttpMethod
import router.definition.ResourceThrottlingTier.ResourceThrottlingTier
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

object AuthType extends Enumeration {
  type AuthType = Value
  val NONE, APPLICATION, USER = Value

  implicit val formatAuthType: Format[AuthType] = EnumJson.enumFormat(AuthType)
}

object HttpMethod extends Enumeration {
  type HttpMethod = Value
  val GET, POST, PUT = Value

  implicit val formatHttpMethod: Format[HttpMethod] = EnumJson.enumFormat(HttpMethod)
}

object ResourceThrottlingTier extends Enumeration {
  type ResourceThrottlingTier = Value
  val UNLIMITED = Value

  implicit val formatResourceThrottlingTier = EnumJson.enumFormat(ResourceThrottlingTier)
}

object GroupName extends Enumeration {
  type GroupName = Value
  val SelfEmployments = Value("Self Employment Businesses")
  val UKProperties = Value("UK Property Business")
  val Dividends = Value("Dividends Income")
  val BankSavings = Value("Savings Accounts")
  val Calculation = Value("Tax Calculations")

  implicit val formatGroupName = EnumJson.enumFormat(GroupName)
}

case class Endpoint(uriPattern: String,
                    endpointName: String,
                    method: HttpMethod,
                    authType: AuthType,
                    throttlingTier: ResourceThrottlingTier,
                    scope: Option[String] = None,
                    groupName : GroupName,
                    queryParameters: Option[Seq[Parameter]] = None)

object Endpoint {
  implicit val formatEndpoint = Json.format[Endpoint]
}

case class APIVersion(
                       version: String,
                       access: Option[Access] = None,
                       status: APIStatus,
                       endpointsEnabled: Boolean,
                       endpoints: Seq[Endpoint])

object APIVersion {
  implicit val formatAPIVersion = Json.format[APIVersion]
}

case class APIDefinition(name: String,
                         description: String,
                         context: String,
                         versions: Seq[APIVersion],
                         requiresTrust: Option[Boolean]) {

  require(name.nonEmpty, s"name is required")
  require(context.nonEmpty, s"context is required")
  require(description.nonEmpty, s"description is required")
  require(versions.nonEmpty, s"at least one version is required")
  require(uniqueVersions, s"version numbers must be unique")
  versions.foreach(version => {
    require(version.version.nonEmpty, s"version is required")
    version.endpoints.foreach(endpoint => {
      require(endpoint.endpointName.nonEmpty, s"endpointName is required")
      endpoint.queryParameters.getOrElse(Nil).foreach(parameter => {
        require(parameter.name.nonEmpty, "parameter name is required")
      })
      endpoint.authType match {
        case AuthType.USER => require(endpoint.scope.nonEmpty, s"scope is required if authType is USER")
        case _ => ()
      }
    })
  })

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