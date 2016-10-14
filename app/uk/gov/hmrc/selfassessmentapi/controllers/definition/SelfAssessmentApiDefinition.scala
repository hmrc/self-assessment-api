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

import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{FeatureSwitchedTaxYearProperties, SourceType, SourceTypes}
import uk.gov.hmrc.selfassessmentapi.controllers.definition.APIStatus.APIStatus
import uk.gov.hmrc.selfassessmentapi.controllers.definition.AuthType._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.GroupName._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.HttpMethod._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.ResourceThrottlingTier._
import uk.gov.hmrc.selfassessmentapi.views.Helpers

class SelfAssessmentApiDefinition(apiContext: String, apiStatus: APIStatus) {

  private val readScope = "read:self-assessment"
  private val writeScope = "write:self-assessment"

  val firstEndpoints: Seq[Endpoint] = {
    Seq(Endpoint(
      uriPattern = "/",
      endpointName = "Resolve Taxpayer",
      method = GET,
      authType = USER,
      throttlingTier = UNLIMITED,
      scope = Some(readScope),
      groupName = Taxpayer
    ),
    Endpoint(
      uriPattern = "/{utr}",
      endpointName = "Discover Tax Years",
      method = GET,
      authType = USER,
      throttlingTier = UNLIMITED,
      scope = Some(readScope),
      groupName = Taxpayer
    ),
    Endpoint(
      uriPattern = "/{utr}/{tax-year}",
      endpointName = "Discover Tax Year",
      method = GET,
      authType = USER,
      throttlingTier = UNLIMITED,
      scope = Some(readScope),
      groupName = Taxpayer
    ))
  }

  private val lastEndpoints: Seq[Endpoint] = {
    Seq(Endpoint(
      uriPattern = "/{utr}/{tax-year}/liability",
      endpointName = "Request Liability",
      method = POST,
      authType = USER,
      throttlingTier = UNLIMITED,
      scope = Some(writeScope),
      groupName = Liability
    ),
    Endpoint(
      uriPattern = "/{utr}/{tax-year}/liability",
      endpointName = "Retrieve Liability",
      method = GET,
      authType = USER,
      throttlingTier = UNLIMITED,
      scope = Some(readScope),
      groupName = Liability
    ))
  }

  private val switchedEndpoints = {
    if (FeatureSwitchedTaxYearProperties.atLeastOnePropertyIsEnabled) {
      Seq(
        Endpoint(
          uriPattern = "/{utr}/{tax-year}",
          endpointName = "Update Tax Year",
          method = PUT,
          authType = USER,
          throttlingTier = UNLIMITED,
          scope = Some(writeScope),
          groupName = Taxpayer
        )
      )
    }
    else Seq()
  }

  private val allEndpoints = firstEndpoints ++ switchedEndpoints ++ sourceAndSummaryEndpoints ++ lastEndpoints


  val definition: Definition =
    Definition(
      scopes = Seq(
        Scope(
          key = readScope,
          name = "View your Self-Assessment information",
          description = "Allow read access to self assessment data"
        ),
        Scope(
          key = writeScope,
          name = "Change your Self-Assessment information",
          description = "Allow write access to self assessment data"
        )
      ),
      api = APIDefinition(
        name = "Self Assessment",
        description = "An API for providing self assessment data and obtaining liability estimations",
        context = apiContext,
        versions = Seq(
          APIVersion(
            version = "1.0",
            access = buildWhiteListingAccess(),
            status = apiStatus,
            endpoints = allEndpoints
          )
        ),
        requiresTrust = None
      )
    )

  private lazy val resolveGroupName : SourceType => GroupName = {
    case SourceTypes.Employments => Employments
    case SourceTypes.SelfEmployments => SelfEmployments
    case SourceTypes.UKProperties => UKProperties
    case SourceTypes.FurnishedHolidayLettings => FurnishedHolidayLettings
    case SourceTypes.UnearnedIncomes => UnearnedIncomes
    case SourceTypes.Dividends => Dividends
    case SourceTypes.Banks => Banks
  }

  private lazy val sourceAndSummaryEndpoints = Helpers.enabledSourceTypes.toSeq.flatMap { sourceType =>
    val uri: String = s"/{utr}/{tax-year}/${sourceType.name}"
    val uriWithId: String = s"$uri/{${sourceType.name}-id}"
    val updateEndpoint =  sourceType match  {
      case SourceTypes.Employments | SourceTypes.UnearnedIncomes | SourceTypes.Banks | SourceTypes.Dividends =>  Nil
      case _ => Seq(Endpoint(uriPattern = uriWithId, endpointName = s"Update ${sourceType.documentationName}", method = PUT,
        authType = USER, throttlingTier = UNLIMITED, scope = Some(writeScope), groupName =  resolveGroupName(sourceType)))
    }
    Seq(
      Endpoint(uriPattern = uri, endpointName = s"Create ${sourceType.documentationName}", method = POST,
        authType = USER, throttlingTier = UNLIMITED, scope = Some(writeScope), groupName =  resolveGroupName(sourceType)),
      Endpoint(uriPattern = uriWithId, endpointName = s"Retrieve ${sourceType.documentationName}", method = GET,
        authType = USER, throttlingTier = UNLIMITED, scope = Some(readScope), groupName =  resolveGroupName(sourceType)),
      Endpoint(uriPattern = uriWithId, endpointName = s"Delete ${sourceType.documentationName}", method = DELETE,
        authType = USER, throttlingTier = UNLIMITED, scope = Some(writeScope), groupName =  resolveGroupName(sourceType)),
      Endpoint(uriPattern = uri, endpointName = s"Retrieve All ${sourceType.documentationName}", method = GET,
        authType = USER, throttlingTier = UNLIMITED, scope = Some(readScope), groupName =  resolveGroupName(sourceType))
    )  ++ updateEndpoint ++  summaryEndpoints(sourceType)
  }

  private lazy val summaryEndpoints : SourceType => Seq[Endpoint]  = { sourceType =>
    Helpers.enabledSummaries(sourceType).toSeq.flatMap { summaryType =>
      val uri: String = s"/{utr}/{tax-year}/${sourceType.name}/{${sourceType.name}-id}/${summaryType.name}"
      val uriWithId: String = s"$uri/{${summaryType.name}-id}"
      Seq(
        Endpoint(uriPattern = uri, endpointName = s"Create ${sourceType.documentationName} ${summaryType.documentationName}", method = POST,
          authType = USER, throttlingTier = UNLIMITED, scope = Some(writeScope), groupName =  resolveGroupName(sourceType)),
        Endpoint(uriPattern = uriWithId, endpointName = s"Retrieve ${sourceType.documentationName} ${summaryType.documentationName}", method = GET,
          authType = USER, throttlingTier = UNLIMITED, scope = Some(readScope), groupName =  resolveGroupName(sourceType)),
        Endpoint(uriPattern = uriWithId, endpointName = s"Update ${sourceType.documentationName} ${summaryType.documentationName}", method = PUT,
          authType = USER, throttlingTier = UNLIMITED, scope = Some(writeScope), groupName =  resolveGroupName(sourceType)),
        Endpoint(uriPattern = uriWithId, endpointName = s"Delete ${sourceType.documentationName} ${summaryType.documentationName}", method = DELETE,
          authType = USER, throttlingTier = UNLIMITED, scope = Some(writeScope), groupName =  resolveGroupName(sourceType)),
        Endpoint(uriPattern = uri, endpointName = s"Retrieve All ${sourceType.documentationName} ${summaryType.documentationName}", method = GET,
          authType = USER, throttlingTier = UNLIMITED, scope = Some(readScope), groupName =  resolveGroupName(sourceType))
      )
    }
  }


  private def buildWhiteListingAccess(): Option[Access] = {
    val featureSwitch = FeatureSwitch(AppContext.featureSwitch)
    featureSwitch.isWhiteListingEnabled match {
      case true =>  Some(Access("PRIVATE", featureSwitch.whiteListedApplicationIds))
      case false => None
    }
  }
}

object PublishedSelfAssessmentApiDefinition extends SelfAssessmentApiDefinition(AppContext.apiGatewayRegistrationContext, APIStatus.PUBLISHED)

object PrototypedSelfAssessmentApiDefinition extends SelfAssessmentApiDefinition(AppContext.apiGatewayRegistrationContext, APIStatus.PROTOTYPED)
