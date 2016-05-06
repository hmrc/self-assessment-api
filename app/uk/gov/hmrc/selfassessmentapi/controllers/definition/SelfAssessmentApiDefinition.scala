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

package uk.gov.hmrc.selfassessmentapi.controllers.definition

import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.definition.APIStatus.APIStatus

class SelfAssessmentApiDefinition(apiContext: String, apiStatus: APIStatus) {

  private val readScope = "read:self-assessment-api"
  private val writeScope = "write:self-assessment-api"

  val definition: Definition =
    Definition(
      scopes = Seq(
        Scope(
          key = readScope,
          name = "Self-Assessment API - Read",
          description = "<TODO>"
        ),
        Scope(
          key = writeScope,
          name = "Self-Assessment API - Write",
          description = "<TODO>"
        )
      ),
      api = APIDefinition(
        name = "Self-Assessment API",
        description = "<TODO>",
        context = apiContext,
        versions = Seq(
          APIVersion(
            version = "1.0",
            status = apiStatus,
            endpoints = Seq(
              Endpoint(
                uriPattern = "/",
                endpointName = "Resolve Customer",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}",
                endpointName = "Discover",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/self-employments/{selfEmploymentsId}",
                endpointName = "Retrieve Self Employment",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/self-employments",
                endpointName = "Create Self Employment",
                method = HttpMethod.POST,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/self-employments/{selfEmploymentsId}",
                endpointName = "Update Self Employment",
                method = HttpMethod.PUT,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/self-employments/{selfEmploymentsId}",
                endpointName = "Delete Self Employment",
                method = HttpMethod.DELETE,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/liabilities",
                endpointName = "Request Liability",
                method = HttpMethod.POST,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/liabilities/{liabilityId}",
                endpointName = "Retrieve Liability",
                method = HttpMethod.GET,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(readScope)
              ),
              Endpoint(
                uriPattern = "/{utr}/liabilities/{liabilityId}",
                endpointName = "Delete Liability",
                method = HttpMethod.DELETE,
                authType = AuthType.USER,
                throttlingTier = ResourceThrottlingTier.UNLIMITED,
                scope = Some(writeScope)
              )
            )
          )
        ),
        requiresTrust = None
      )
    )
}

object PublishedSelfAssessmentApiDefinition extends SelfAssessmentApiDefinition(AppContext.apiGatewayContext, APIStatus.PUBLISHED)

object PrototypedSelfAssessmentApiDefinition extends SelfAssessmentApiDefinition(AppContext.apiGatewayContext, APIStatus.PROTOTYPED)
