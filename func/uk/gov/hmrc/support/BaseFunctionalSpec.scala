package uk.gov.hmrc.support

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.{JsArray, JsObject, JsValue, Reads}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.TestApplication
import uk.gov.hmrc.selfassessmentapi.controllers.ErrorNotImplemented
import uk.gov.hmrc.selfassessmentapi.domain.{SourceType, SourceTypes}

import scala.util.matching.Regex

trait BaseFunctionalSpec extends TestApplication {

  protected val saUtr = generateSaUtr()
  val taxYear = "2016-17"

  class Assertions(request: String, response: HttpResponse) {
    def bodyHasSummaryLinks(sourceType: SourceType, sourceId: String, saUtr: SaUtr, taxYear: String) = {
      sourceType.summaryTypes.foreach { summaryType =>
        bodyHasLink(summaryType.name, s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}".r)
      }
    }

    def bodyHasSummaryLinks(sourceType: SourceType, saUtr: SaUtr, taxYear: String) = {
      sourceType.summaryTypes.foreach { summaryType =>
        bodyHasLink(summaryType.name, s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+/${summaryType.name}".r)
      }
      this
    }

    def bodyHasLinksForAllSourceTypes(saUtr: SaUtr, taxYear: String) = {
      SourceTypes.types.foreach { sourceType =>
        bodyHasLink(sourceType.name, s"/self-assessment/$saUtr/$taxYear/${sourceType.name}")
      }
      this
    }

    def resourceIsNotImplemented() = {
      statusIs(501)
        .contentTypeIsJson()
        .body(_ \ "code").is(ErrorNotImplemented.errorCode)
        .body(_ \ "message").is(ErrorNotImplemented.message)
      this
    }

    def contentTypeIsXml() = contentTypeIs("application/xml")

    def contentTypeIsJson() = contentTypeIs("application/json; charset=utf-8")

    def contentTypeIsHalJson() = contentTypeIs("application/hal+json")

    def bodyIs(expectedBody: String) = {
      response.body shouldBe expectedBody
      this
    }

    def bodyIs(expectedBody: JsValue) = {
      (response.json match {
        case JsObject(fields) => response.json.as[JsObject]  - "_links" - "id"
        case json => json
      }) shouldEqual expectedBody
      this
    }

    def bodyHasLink(rel: String, href: String) = {
      getLinkFromBody(rel) shouldEqual Some(href)
      this
    }

    def bodyHasPath[T](path: String, value: T)(implicit reads: Reads[T]) : Assertions = {
      extractPathElement(path) shouldEqual Some(value)
      this
    }

    def bodyHasPath(path: String, valuePattern: Regex) = {
      extractPathElement[String](path) match {
        case Some(x) => valuePattern findFirstIn x match {
          case Some(v) =>
          case None => fail(s"$x did not match pattern")
        }
        case unknown => fail(s"No value found for $path")
      }
      this
    }

    private def extractPathElement[T](path: String)(implicit reads: Reads[T]): Option[T] = {
      val pathSeq = path.filter(!_.isWhitespace).split('\\').toSeq.filter(!_.isEmpty)
      def op(js: JsValue, pathElement: String) = {
        val pattern = """(.*)\((\d+)\)""".r
        pathElement match {
          case pattern(arrayName, index) =>
            if (arrayName.isEmpty) js(index.toInt) else (js \ arrayName) (index.toInt)
          case _ => js \ pathElement
        }
      }
      pathSeq.foldLeft(response.json)(op).asOpt[T]
    }


    private def getLinkFromBody(rel: String): Option[String] = {
      val links = response.json \ "_links"
      val link = links \ rel
      (link \ "href").asOpt[String]
    }

    def bodyHasLink(rel: String, hrefPattern: Regex) = {
      getLinkFromBody(rel) match {
        case Some(href) => hrefPattern findFirstIn href match {
          case Some(v) =>
          case None => fail(s"$href did not match pattern")
        }
        case unknown => fail(s"No href found for $rel")
      }
      this
    }

    def statusIs(statusCode: Int) = {
      withClue(s"expected $request to return $statusCode; but got ${response.body}\n") { response.status shouldBe statusCode }
      this
    }

    private def contentTypeIs(contentType: String) = {
      response.header("Content-Type") shouldEqual Some(contentType)
      this
    }

    def body(myQuery: JsValue => JsValue) = {
      new BodyAssertions(myQuery(response.json), this)
    }

    def body1(myQuery: JsValue => Seq[JsValue]) = {
      new BodyListAssertions(myQuery(response.json), this)
    }

    class BodyAssertions(content: JsValue, assertions: Assertions) {
      def is(value: String) = {
        content.as[String] shouldBe value
        assertions
      }

      def is(value: BigDecimal) = {
        content.as[BigDecimal] shouldBe value
        assertions
      }
    }

    class BodyListAssertions(content: Seq[JsValue], assertions: Assertions) {
      def is(value: String*) = {
        content.map(con => con.as[String]) should contain theSameElementsAs value
        assertions
      }
    }

  }

  class HttpRequest(method: String, path: String, body: Option[JsValue]) {
    assert(path.startsWith("/"), "please provide only a path starting with '/'")
    var addAcceptHeader = true
    var hc = HeaderCarrier()
    val url = s"http://localhost:$port$path"

    def withoutAcceptHeader() = {
      this.addAcceptHeader = false
      this
    }

    def thenAssertThat() = {
      if (addAcceptHeader) hc = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.1.0+json"))

      withClue(s"Request $method $url") {
        method match {
          case "GET" => new Assertions(s"GET@$url", Http.get(url)(hc))
          case "DELETE" => new Assertions(s"DELETE@$url", Http.delete(url)(hc))
          case "POST" => {
            body match {
              case Some(jsonBody) => new Assertions(s"POST@$url", Http.postJson(url, jsonBody)(hc))
              case None => new Assertions(s"POST@$url",Http.postEmpty(url)(hc))
            }
          }
          case "PUT" => {
            val jsonBody = body.getOrElse(throw new RuntimeException("Body for PUT must be provided"))
            new Assertions(s"PUT@$url", Http.putJson(url, jsonBody)(hc))
          }
        }
      }
    }

    def withAcceptHeader() = {
      addAcceptHeader = true
      this
    }
  }

  class HttpPostBodyWrapper(method: String, body: Option[JsValue]) {
    def to(url: String) = new HttpRequest(method, url, body)
  }

  class HttpPutBodyWrapper(method: String, body: Option[JsValue]) {
    def at(url: String) = new HttpRequest(method, url, body)
  }

  class HttpVerbs {
    def post(body: Some[JsValue]) = {
      new HttpPostBodyWrapper("POST", body)
    }

    def put(body: Some[JsValue]) = {
      new HttpPutBodyWrapper("PUT", body)
    }

    def get(path: String) = {
      new HttpRequest("GET", path, None)
    }

    def delete(path: String) = {
      new HttpRequest("DELETE", path, None)
    }

    def post(path: String, body: Option[JsValue] = None) = {
      new HttpRequest("POST", path, body)
    }

    def put(path: String, body: Option[JsValue]) = {
      new HttpRequest("PUT", path, body)
    }

  }

  class Givens {
    def when() = new HttpVerbs()

    def userIsNotAuthorisedForTheResource(utr: SaUtr) = {
      stubFor(get(urlPathEqualTo(s"/authorise/read/sa/$utr")).willReturn(aResponse().withStatus(401).withHeader("Content-Length", "0")))
      stubFor(get(urlPathEqualTo(s"/authorise/write/sa/$utr")).willReturn(aResponse().withStatus(401).withHeader("Content-Length", "0")))
      this
    }

    def userIsAuthorisedForTheResource(utr: SaUtr) = {
      stubFor(get(urlPathEqualTo(s"/authorise/read/sa/$utr")).willReturn(aResponse().withStatus(200)))
      stubFor(get(urlPathEqualTo(s"/authorise/write/sa/$utr")).willReturn(aResponse().withStatus(200)))
      this
    }

    def userIsEnrolledInSa(utr: SaUtr) = {
      val json =
        s"""
           |{
           |    "accounts": {
           |        "sa": {
           |            "link": "/sa/individual/$utr",
           |            "utr": "$utr"
           |        }
           |    },
           |    "confidenceLevel": 500
           |}
      """.stripMargin

      stubFor(get(urlPathEqualTo(s"/auth/authority")).willReturn(aResponse().withBody(json).withStatus(200).withHeader("Content-Type",
        "application/json")))
      this
    }

    def userIsNotEnrolledInSa = {
      val json =
        s"""
           |{
           |    "accounts": {
           |    },
           |    "confidenceLevel": 500
           |}
      """.stripMargin

      stubFor(get(urlPathEqualTo(s"/auth/authority")).willReturn(aResponse().withBody(json).withStatus(200).withHeader("Content-Type",
        "application/json")))
      this
    }


  }

  def given() = new Givens()

  def when() = new HttpVerbs()

}


