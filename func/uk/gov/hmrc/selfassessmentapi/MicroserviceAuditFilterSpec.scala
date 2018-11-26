package uk.gov.hmrc.r2.selfassessmentapi

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{urlPathEqualTo, _}
import org.scalatest.time.{Millis, Seconds, Span}
import uk.gov.hmrc.r2.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class MicroserviceAuditFilterSpec extends BaseFunctionalSpec {

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  "Audit filter" should {
    "be applied when a POST request is made" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)

      eventually {
        WireMock.findAll(postRequestedFor(urlPathEqualTo("/write/audit"))
        ).size shouldBe 1
      }
    }
  }

}
