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

package fixtures

import play.api.libs.json.Json
import router.definition.AuthType.USER
import router.definition.GroupName.SelfEmployments
import router.definition.HttpMethod.GET
import router.definition.ResourceThrottlingTier.UNLIMITED
import router.definition._

object SelfAssessmentApiDefinitionFixture {

  private val scopeKey = "test-scope"
  private val scopeName = "test scope name"
  private val scopeDescription = "test scope description"

  private val apiName = "test api name"
  private val apiDescription = "test api description"
  private val apiContext = "test api context"

  private val accessType = "test type"

  val selfAssessmentApiDefinition = Definition(
    scopes = Seq(
      Scope(
        key = scopeKey,
        name = scopeName,
        description = scopeDescription
      )
    ),
    api = APIDefinition(
      name = apiName,
      description = apiDescription,
      context = apiContext,
      versions = Seq(APIVersion(
        version = "1.0",
        access = Some(Access(
          `type` = accessType,
          whitelistedApplicationIds = Seq("test-whitelisted-id")
        )),
        status = APIStatus.ALPHA,
        endpointsEnabled = true,
        endpoints = Seq(
          Endpoint(
            uriPattern = "/test/uri/pattern",
            endpointName = "test endpoint name",
            method = GET,
            authType = USER,
            throttlingTier = UNLIMITED,
            scope = Some("test-scope"),
            groupName = SelfEmployments)
        )
      )),
      requiresTrust = None
    )
  )

  val selfAssessmentApiDefinitionJson = {
    Json.obj(
      "scopes" -> Json.arr(Json.obj(
        "key" -> scopeKey,
        "name" -> scopeName,
        "description" -> scopeDescription
      )),
      "api" -> Json.obj(
        "name" -> apiName,
        "description" -> apiDescription,
        "context" -> apiContext,
        "versions" -> Json.arr(Json.obj(
          "version" -> "1.0",
          "access" -> Json.obj(
            "type" -> accessType,
            "whitelistedApplicationIds" -> Json.arr("test-whitelisted-id")
          ),
          "status" -> "ALPHA",
          "endpointsEnabled" -> true,
          "endpoints" -> Json.arr(Json.obj(
            "uriPattern" -> "/test/uri/pattern",
            "endpointName" -> "test endpoint name",
            "method" -> "GET",
            "authType" -> "USER",
            "throttlingTier" -> "UNLIMITED",
            "scope" -> scopeKey,
            "groupName" -> "Self Employment Businesses"
          ))
        ))
      )
    )
  }
}
