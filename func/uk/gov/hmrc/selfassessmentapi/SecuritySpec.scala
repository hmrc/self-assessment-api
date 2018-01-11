package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec
import play.api.libs.json.JsString
import uk.gov.hmrc.selfassessmentapi.resources.Jsons

class SecuritySpec extends BaseFunctionalSpec {

  "Security" should {

    "not include internal software details with invalid JSON payloads" in {
      val invalidJson = """
        {
          "from" "2017-07-06",
          "to": "2017-07-07",
          "incomes”: {
            "turnover": {
              "amount": 100.25
            },
            "other": {
            "amount”": 100.25
            }
          },
          "consolidatedExpenses": ['1',1]
        }"""

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .des()
        .selfEmployment
        .periodWillBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when().post(s"%sourceLocation%/periods", invalidJson, "application/json")
        .thenAssertThat()
        .bodyDoesNotHaveString("akka.util.ByteIterator")
        .bodyDoesNotHaveString("ByteArrayIterator")
        .statusIs(400)
    }

  }

}
