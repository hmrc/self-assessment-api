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

package uk.gov.hmrc.selfassessmentapi.contexts

sealed trait AuthContext {
  val affinityGroup: String
  val agentCode: Option[String]
}

case object Individual extends AuthContext {
  override val affinityGroup: String = "individual"
  override val agentCode: Option[String] = None
}

case class Agent(override val agentCode: Option[String]) extends AuthContext {
  override val affinityGroup: String = "agent"
}

case class FilingOnlyAgent(override val agentCode: Option[String]) extends AuthContext {
  override val affinityGroup: String = "filingOnlyAgent"
}