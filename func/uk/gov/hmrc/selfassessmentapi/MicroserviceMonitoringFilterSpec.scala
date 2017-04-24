package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class MicroserviceMonitoringFilterSpec extends BaseFunctionalSpec {

  "Monitoring filter" should {
    "be applied when requests are made" in {
      given()
        .userIsFullyAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeCreatedFor(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .when()
        .get("/admin/metrics")
        .thenAssertThat()
        .body(_ \ "timers" \ "Timer-API-SelfEmployments-POST" \ "count").is(1)
        .body(_ \ "timers" \ "Timer-API-SelfEmployments-GET" \ "count").is(1)
    }
  }

}
