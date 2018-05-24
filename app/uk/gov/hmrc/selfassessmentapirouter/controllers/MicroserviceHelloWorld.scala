package uk.gov.hmrc.selfassessmentapirouter.controllers

import javax.inject.Singleton

import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.mvc._

import scala.concurrent.Future

@Singleton()
class MicroserviceHelloWorld extends BaseController {

	def hello() = Action.async { implicit request =>
		Future.successful(Ok("Hello world"))
	}

}