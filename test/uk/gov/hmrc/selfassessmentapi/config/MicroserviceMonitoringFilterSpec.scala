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

package uk.gov.hmrc.selfassessmentapi.config

import com.kenshoo.play.metrics.Metrics
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.models.SourceType.sourceTypeToDocumentationName
import uk.gov.hmrc.selfassessmentapi.models.TaxYear.taxYearFormat

class MicroserviceMonitoringFilterSpec extends UnitSpec with MockitoSugar{

  val filter = new MicroserviceMonitoringFilter(mock[Metrics])

  private def translate(summary: String) = summary match {
    case x if x.matches(taxYearFormat) => "annuals"
    case x => x
  }

  "Monitoring filter on source level" should {
    for (source <- filter.sources) {
      for (method <- Seq("GET", "POST", "PUT")) {
        s"work for $source and $method" in {
          filter.apiName(s"/ni/PL492785D/$source", method).get shouldEqual s"API-${sourceTypeToDocumentationName(source)}-$method"
          filter.apiName(s"/ni/PL492785D/$source/", method).get shouldEqual s"API-${sourceTypeToDocumentationName(source)}-$method"
          filter.apiName(s"/ni/PL492785D/$source/234567453", method).get shouldEqual s"API-${sourceTypeToDocumentationName(source)}-id-$method"
          filter.apiName(s"/ni/PL492785D/$source/234567453/", method).get shouldEqual s"API-${sourceTypeToDocumentationName(source)}-id-$method"
        }
      }
    }
  }

  "Monitoring filter on summary level" should {
    for (source <- filter.sources) {
      for (method <- Seq("GET", "POST", "PUT")) {
        for (summary <- Seq("periods")) {
          s"work for $source and $summary and $method" in {
            filter.apiName(s"/ni/PL492785D/$source/123456/$summary", method).get shouldEqual s"API-${sourceTypeToDocumentationName(source)}-$summary-$method"
            filter.apiName(s"/ni/PL492785D/$source/123456/$summary/", method).get shouldEqual s"API-${sourceTypeToDocumentationName(source)}-$summary-$method"
            filter.apiName(s"/ni/PL492785D/$source/123456/$summary/77777777", method).get shouldEqual s"API-${sourceTypeToDocumentationName(source)}-$summary-id-$method"
            filter.apiName(s"/ni/PL492785D/$source/123456/$summary/77777777/", method).get shouldEqual s"API-${sourceTypeToDocumentationName(source)}-$summary-id-$method"
          }
        }

        for (summary <- Seq("obligations", "2017-18")) {
          s"work for $source and $summary and $method" in {
            filter.apiName(s"/ni/PL492785D/$source/123456/$summary", method).get shouldEqual s"API-${sourceTypeToDocumentationName(source)}-${translate(summary)}-$method"
            filter.apiName(s"/ni/PL492785D/$source/123456/$summary/", method).get shouldEqual s"API-${sourceTypeToDocumentationName(source)}-${translate(summary)}-$method"
          }
        }
      }
    }
  }
}
