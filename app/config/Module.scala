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

package config

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import config.locator.ServiceLocatorRegistration
import router.connectors.{SelfAssessmentConnector, TaxCalcConnector}
import router.services.{SelfAssessmentService, SelfAssessmentServiceT, TaxCalcService, TaxCalcServiceT}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[ServiceLocatorRegistration]).asEagerSingleton()
    bind(classOf[SelfAssessmentServiceT]).to(classOf[SelfAssessmentService])
    bind(classOf[SelfAssessmentConnector]).annotatedWith(Names.named("self-assessment-1.0")).to(classOf[SelfAssessmentConnector])
    bind(classOf[TaxCalcServiceT]).to(classOf[TaxCalcService])
    bind(classOf[TaxCalcConnector]).annotatedWith(Names.named("tax-calc-2.0")).to(classOf[TaxCalcConnector])
  }
}
