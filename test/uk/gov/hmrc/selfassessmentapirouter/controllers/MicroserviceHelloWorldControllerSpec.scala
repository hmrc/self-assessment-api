package uk.gov.hmrc.selfassessmentapirouter.controllers

import play.api.http.Status
import play.api.test.FakeRequest
import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.test.WithFakeApplication
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}


class MicroserviceHelloWorldControllerSpec extends UnitSpec with WithFakeApplication{

  val fakeRequest = FakeRequest("GET", "/")

  "GET /" should {
    "return 200" in {
      val controller = new MicroserviceHelloWorld()
      val result = controller.hello()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

}
