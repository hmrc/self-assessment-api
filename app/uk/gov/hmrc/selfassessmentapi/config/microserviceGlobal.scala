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

package uk.gov.hmrc.selfassessmentapi.config

import javax.inject.Inject

import com.kenshoo.play.metrics.Metrics
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.{StringReader, ValueReader}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Application, Configuration, Play}
import play.routing.Router.Tags
import uk.gov.hmrc.api.config.{ServiceLocatorConfig, ServiceLocatorRegistration}
import uk.gov.hmrc.api.connector.ServiceLocatorConnector
import uk.gov.hmrc.api.controllers.{ErrorAcceptHeaderInvalid, ErrorNotFound, ErrorUnauthorized, HeaderValidator}
import uk.gov.hmrc.kenshoo.monitoring.MonitoringFilter
import uk.gov.hmrc.play.audit.filters.AuditFilter
import uk.gov.hmrc.play.auth.controllers.{AuthConfig, AuthParamsControllerConfig}
import uk.gov.hmrc.play.auth.microservice.connectors._
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
import uk.gov.hmrc.play.config.{AppName, RunMode}
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import uk.gov.hmrc.play.http.{HeaderCarrier, NotImplementedException}
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal
import uk.gov.hmrc.play.scheduling._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{ErrorCode, SourceTypes}
import uk.gov.hmrc.selfassessmentapi.controllers.{ErrorBadRequest, ErrorNotImplemented}
import uk.gov.hmrc.selfassessmentapi.jobs.{DeleteExpiredDataJob, DropMongoCollectionJob}
import uk.gov.hmrc.selfassessmentapi.services.errors.{BusinessError, BusinessException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex

case class ControllerConfigParams(needsHeaderValidation: Boolean = true, needsLogging: Boolean = true,
                                  needsAuditing: Boolean = true, needsAuth: Boolean = true, needsTaxYear: Boolean = true)

object ControllerConfiguration {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
  implicit val regexValueReader: ValueReader[Regex] = StringReader.stringValueReader.map(_.r)

  implicit val controllerParamsReader = ValueReader.relative[ControllerConfigParams] { config =>
    ControllerConfigParams(
      needsHeaderValidation = config.getAs[Boolean]("needsHeaderValidation").getOrElse(true),
      needsLogging = config.getAs[Boolean]("needsLogging").getOrElse(true),
      needsAuditing = config.getAs[Boolean]("needsAuditing").getOrElse(true),
      needsAuth = config.getAs[Boolean]("needsAuth").getOrElse(true),
      needsTaxYear = config.getAs[Boolean]("needsTaxYear").getOrElse(true)
    )
  }

  def controllerParamsConfig(controllerName: String): ControllerConfigParams = {
    controllerConfigs.as[Option[ControllerConfigParams]](controllerName).getOrElse(ControllerConfigParams())
  }
}


object AuthParamsControllerConfiguration extends AuthParamsControllerConfig {
  lazy val controllerConfigs = ControllerConfiguration.controllerConfigs
}

object MicroserviceAuditFilter extends AuditFilter with AppName with MicroserviceFilterSupport {
  override val auditConnector = MicroserviceAuditConnector

  override def controllerNeedsAuditing(controllerName: String) = AppContext.auditEnabled && ControllerConfiguration.controllerParamsConfig(controllerName).needsAuditing
}

object MicroserviceLoggingFilter extends LoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.controllerParamsConfig(controllerName).needsLogging
}



class MicroserviceMonitoringFilter @Inject()(metrics: Metrics) extends MonitoringFilter with MicroserviceFilterSupport {
  override lazy val urlPatternToNameMapping = SourceTypes.types.map(sourceType => s".*[/]${sourceType.name}[/]?.*" -> sourceType.documentationName.replaceAll("\\s", "")).toMap
  override def kenshooRegistry = metrics.defaultRegistry
}

object MicroserviceAuthFilter extends AuthorisationFilter with MicroserviceFilterSupport {
  override lazy val authParamsConfig = AuthParamsControllerConfiguration
  override lazy val authConnector = MicroserviceAuthConnector

  override def extractResource(pathString: String, verb: HttpVerb, authConfig: AuthConfig): Option[ResourceToAuthorise] = {
    authConfig.mode match {
      case "identity" => extractIdentityResource(pathString, verb, authConfig)
      case "passcode" => super.extractResource(pathString, verb, authConfig)
    }
  }

  private def extractIdentityResource(pathString: String, verb: HttpVerb, authConfig: AuthConfig): Option[ResourceToAuthorise] = {
    pathString match {
      case authConfig.pattern(nino) =>
        Some(RegimeAndIdResourceToAuthorise(verb, Regime("paye"), AccountId(nino)))
      case _ => None
    }
  }

  override def apply(next: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    super.apply(next)(rh) map { res =>
      res.header.status
      match {
        case 401 => Status(ErrorUnauthorized.httpStatusCode)(Json.toJson(ErrorUnauthorized))
        case _ => res
      }
    }
  }

  override def controllerNeedsAuth(controllerName: String): Boolean = AppContext.authEnabled && ControllerConfiguration.controllerParamsConfig(controllerName).needsAuth
}

object HeaderValidatorFilter extends Filter with HeaderValidator with MicroserviceFilterSupport {
  def apply(next: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    val controller = rh.tags.get(Tags.ROUTE_CONTROLLER)
    val needsHeaderValidation = controller.forall(name => ControllerConfiguration.controllerParamsConfig(name).needsHeaderValidation)

    if (!needsHeaderValidation || acceptHeaderValidationRules(rh.headers.get("Accept"))) next(rh)
    else Future.successful(Status(ErrorAcceptHeaderInvalid.httpStatusCode)(Json.toJson(ErrorAcceptHeaderInvalid)))
  }
}

trait MicroserviceRegistration extends ServiceLocatorRegistration with ServiceLocatorConfig {
  override lazy val registrationEnabled: Boolean = AppContext.registrationEnabled
  override val slConnector: ServiceLocatorConnector = ServiceLocatorConnector(WSHttp)
  override implicit val hc: HeaderCarrier = HeaderCarrier()
}

object MicroserviceGlobal extends DefaultMicroserviceGlobal with MicroserviceRegistration  with RunMode with RunningOfScheduledJobs {

  private var application : Application = _

  override val auditConnector = MicroserviceAuditConnector

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"$env.microservice.metrics")

  override val loggingFilter = MicroserviceLoggingFilter

  override val microserviceAuditFilter = MicroserviceAuditFilter

  override val authFilter = Some(MicroserviceAuthFilter)

  override def microserviceFilters: Seq[EssentialFilter] =
    Seq(HeaderValidatorFilter, application.injector.instanceOf[MicroserviceMonitoringFilter]) ++ defaultMicroserviceFilters

  override lazy val scheduledJobs: Seq[ScheduledJob] = createScheduledJobs()

  def createScheduledJobs(): Seq[ExclusiveScheduledJob] = {
    val expiredJobEnabled: Boolean = AppContext.deleteExpiredDataJob.getBoolean("enabled").getOrElse(false)
    val dropMongoJobEnabled: Boolean = AppContext.dropMongoCollectionJob.getBoolean("enabled").getOrElse(false)
    (expiredJobEnabled, dropMongoJobEnabled) match {
      case (true, true) => Seq(DeleteExpiredDataJob, DropMongoCollectionJob)
      case (true, false) => Seq(DeleteExpiredDataJob)
      case (false, true) => Seq(DropMongoCollectionJob)
      case _ => Seq()
    }
  }

  override def onStart(app : Application): Unit = {
    super.onStart(app)
    application = app
  }

  override def onError(request : RequestHeader, ex: Throwable) = {
    super.onError(request, ex).map { result =>
      ex match {
        case ex: BusinessException => Forbidden(Json.toJson(BusinessError(ex.code, ex.message)))
        case _ =>
          ex.getCause match {
            case ex: NotImplementedException => NotImplemented(Json.toJson(ErrorNotImplemented))
            case _ => result
          }
      }
    }
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    super.onBadRequest(request, error).map { result =>
      error match {
        case "ERROR_INVALID_SOURCE_TYPE" => NotFound(Json.toJson(ErrorNotFound))
        case "ERROR_TAX_YEAR_INVALID" => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.TAX_YEAR_INVALID, "Tax year invalid")))
        case _ => result
      }
    }
  }
}
