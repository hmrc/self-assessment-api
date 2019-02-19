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

package uk.gov.hmrc.selfassessmentapi.filters

import akka.stream.Materializer
import com.kenshoo.play.metrics.Metrics
import javax.inject.Inject
import play.api.mvc.{Filter, RequestHeader, Result}
import uk.gov.hmrc.kenshoo.monitoring.MonitoringFilter

import scala.concurrent.Future

//class MonitoringFilter {
//
//}
//class CustomMonitoringFilter @Inject()(implicit val mat: Materializer) extends Filter {
//
//  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = ???
//
//
//}
//
//
//// TODO WHAT NEEDS TO HAPPEN HERE? READ AS NECESSARY
//
//
//class MicroserviceMonitoringFilter @Inject()(metrics: Metrics, implicit val mat: Materializer) extends MonitoringFilter {
//
//  private[config] val sources = Seq("self-employments", "uk-properties", "calculations")
//
//  private val sourceIdLevel =
//    sources.map(source => s".*[/]$source/.+" -> s"${sourceTypeToDocumentationName(source)}-id").toMap
//
//  private val sourceLevel = sources.map(source => s".*[/]$source[/]?" -> sourceTypeToDocumentationName(source)).toMap
//
//  private val summaryLevel = Map("periods[/]?" -> "periods",
//                                 "periods/.+" -> "periods-id",
//                                 "obligations[/]?" -> "obligations",
//                                 s"${taxYearFormat}[/]?" -> "annuals")
//
//  override lazy val urlPatternToNameMapping = (ListMap((for {
//    source <- sources
//    suffix <- summaryLevel
//  } yield s".*[/]$source[/].*[/]${suffix._1}" -> s"${sourceTypeToDocumentationName(source)}-${suffix._2}").toArray: _*)
//    ++ sourceIdLevel
//    ++ sourceLevel)
//
//  override def kenshooRegistry = metrics.defaultRegistry
//}

