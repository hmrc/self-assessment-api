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

package uk.gov.hmrc.selfassessmentapi.controllers.definition

import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.controllers.definition.APIStatus.APIStatus
import uk.gov.hmrc.selfassessmentapi.controllers.definition.AuthType._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.GroupName._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.HttpMethod._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.ResourceThrottlingTier._


class SelfAssessmentApiDefinition {

  private val readScope = "read:self-assessment"
  private val writeScope = "write:self-assessment"

  val selfEmploymentEndpoints: Seq[Endpoint] = {
    Seq(
      Endpoint(
        uriPattern = "/ni/{nino}/self-employments",
        endpointName = "List self employment businesses",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = SelfEmployments)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/self-employments",
        endpointName = "Add a self employment business",
        method = POST,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = SelfEmployments)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/self-employments/{selfEmploymentId}",
        endpointName = "Update self-employment business details",
        method = PUT,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = SelfEmployments)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/self-employments/{selfEmploymentId}",
        endpointName = "Get self employment business",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = SelfEmployments)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/self-employments/{selfEmploymentId}/obligations",
        endpointName = "Retrieve self employment business obligations",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = SelfEmployments)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/self-employments/{selfEmploymentId}/periods",
        endpointName = "List all self employment periods",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = SelfEmployments)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/self-employments/{selfEmploymentId}/periods",
        endpointName = "Create a self employment period",
        method = POST,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = SelfEmployments)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/self-employments/{selfEmploymentId}/periods/{periodId}",
        endpointName = "Get self employment periodic summary",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = SelfEmployments)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/self-employments/{selfEmploymentId}/periods/{periodId}",
        endpointName = "Update self employment periodic summary",
        method = PUT,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = SelfEmployments)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/self-employments/{selfEmploymentId}/{taxYear}",
        endpointName = "Get self employment annual summary",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = SelfEmployments)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/self-employments/{selfEmploymentId}/{taxYear}",
        endpointName = "Update self employment annual summary",
        method = PUT,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = SelfEmployments)

    )
  }

  val ukPropertyEndpoints: Seq[Endpoint] = {
    Seq(
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties",
        endpointName = "Get UK property business",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties",
        endpointName = "Add a UK property business",
        method = POST,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/obligations",
        endpointName = "Retrieve UK property business obligations",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = UKProperties),
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/other/periods",
        endpointName = "List all non FHL UK property periods",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/other/periods",
        endpointName = "Create a non FHL UK property periodic summary",
        method = POST,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/other/periods/{periodId}",
        endpointName = "Get a non FHL UK property periodic summary",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/other/periods/{periodId}",
        endpointName = "Update a non FHL UK property periodic summary",
        method = PUT,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/other/{taxYear}",
        endpointName = "Get non FHL UK property annual summary",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/other/{taxYear}",
        endpointName = "Update non FHL UK property annual summary",
        method = PUT,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/furnished-holiday-lettings/periods",
        endpointName = "List all FHL UK property periods",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/furnished-holiday-lettings/periods",
        endpointName = "Create a FHL UK property period",
        method = POST,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/furnished-holiday-lettings/periods/{periodId}",
        endpointName = "Get a FHL UK property periodic summary",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/furnished-holiday-lettings/periods/{periodId}",
        endpointName = "Update a FHL UK property periodic summary",
        method = PUT,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/furnished-holiday-lettings/{taxYear}",
        endpointName = "Get FHL UK property annual summary",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = UKProperties)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/uk-properties/furnished-holiday-lettings/{taxYear}",
        endpointName = "Update FHL UK property annual summary",
        method = PUT,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = UKProperties)
    )
  }

  val dividendEndpoints: Seq[Endpoint] = {
    Seq(
      Endpoint(
        uriPattern = "/ni/{nino}/dividends/{taxYear}",
        endpointName = "Get dividends annual summary",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = Dividends)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/dividends/{taxYear}",
        endpointName = "Update dividends annual summary",
        method = PUT,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = Dividends)
    )
  }


  val bankSavingEndpoints: Seq[Endpoint] = {
    Seq(
      Endpoint(
        uriPattern = "/ni/{nino}/savings-accounts",
        endpointName = "List all savings accounts",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = BankSavings)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/savings-accounts",
        endpointName = "Add a savings account",
        method = POST,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = BankSavings)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/savings-accounts/{savingsAccountId}",
        endpointName = "Get a savings account",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = BankSavings)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/savings-accounts/{savingsAccountId}",
        endpointName = "Update a savings account",
        method = PUT,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = BankSavings)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/savings-accounts/{savingsAccountId}/{taxYear}",
        endpointName = "Get a savings account annual summary",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = BankSavings)
      ,
      Endpoint(
        uriPattern = "/ni/{nino}/savings-accounts/{savingsAccountId}/{taxYear}",
        endpointName = "Update a savings account annual summary",
        method = PUT,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = BankSavings)
    )
  }

  val calculationEndpoints: Seq[Endpoint] = Seq(
    Endpoint(
      uriPattern = "/ni/{nino}/calculations",
      endpointName = "Trigger a tax calculation",
      method = POST,
      authType = USER,
      throttlingTier = UNLIMITED,
      scope = Some(writeScope),
      groupName = Calculation),
    Endpoint(
      uriPattern = "/ni/{nino}/calculations/{calculationId}",
      endpointName = "Retrieve a tax calculation",
      method = GET,
      authType = USER,
      throttlingTier = UNLIMITED,
      scope = Some(readScope),
      groupName = Calculation
    )
  )

  private val allEndpoints =
    selfEmploymentEndpoints ++ ukPropertyEndpoints ++ dividendEndpoints ++ bankSavingEndpoints ++ calculationEndpoints


  lazy val definition: Definition =
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
        name = "Self Assessment (MTD)",
        description = "An API for providing self assessment data and obtaining tax calculations",
        context = AppContext.apiGatewayRegistrationContext,
        versions = Seq(
          APIVersion(
            version = "1.0",
            access = buildWhiteListingAccess(),
            status = buildAPIStatus(),
            endpoints = allEndpoints
          )
        ),
        requiresTrust = None
      )
    )

  private def buildAPIStatus(): APIStatus = {
    AppContext.apiStatus match {
      case "PUBLISHED" => APIStatus.PUBLISHED
      case _ => APIStatus.PROTOTYPED
    }
  }

  private def buildWhiteListingAccess(): Option[Access] = {
    val featureSwitch = FeatureSwitch(AppContext.featureSwitch)
    featureSwitch.isWhiteListingEnabled match {
      case true => Some(Access("PRIVATE", featureSwitch.whiteListedApplicationIds))
      case false => None
    }
  }
}

object SelfAssessmentApiDefinition extends SelfAssessmentApiDefinition
