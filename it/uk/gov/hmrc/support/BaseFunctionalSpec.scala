package uk.gov.hmrc.support

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers}
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.{JsObject, JsValue}
import uk.gov.hmrc.domain.{SaUtr, SaUtrGenerator}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec

import scala.util.matching.Regex

trait BaseFunctionalSpec extends UnitSpec with Matchers with OneServerPerSuite with Eventually with ScalaFutures
  with BeforeAndAfterEach with IntegrationPatience with BeforeAndAfterAll with MockitoSugar with MongoEmbeddedDatabase {

  val WIREMOCK_PORT = 22222
  val stubHost = "localhost"
  private val saUtrGenerator = new SaUtrGenerator()

  def generateSaUtr(): SaUtr = saUtrGenerator.nextSaUtr

  protected val wiremockBaseUrl: String = s"http://$stubHost:$WIREMOCK_PORT"

  private val wireMockServer = new WireMockServer(wireMockConfig().port(WIREMOCK_PORT))

  override def beforeAll() = {
    mongoStart()
    wireMockServer.stop()
    wireMockServer.start()
    WireMock.configureFor(stubHost, WIREMOCK_PORT)
    // the below stub is here so that the application finds the registration endpoint which is called on startup
    stubFor(post(urlPathEqualTo("/registration")).willReturn(aResponse().withStatus(200)))
  }

  override def afterAll() = {
    mongoStop()
  }

  override def beforeEach() = {
    WireMock.reset()
  }

  class Assertions(response: HttpResponse) {
    def bodyIs(expectedBody: String) = {
      response.body shouldBe expectedBody
      this
    }

    def bodyIs(expectedBody: JsValue) = {
      response.json.as[JsObject] - "_links" shouldEqual expectedBody
      this
    }

    def bodyHasLink(rel: String, href: String) = {
      getLinkFromBody(rel) shouldEqual Some(href)
      this
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
      response.status shouldBe statusCode
      this
    }

    def contentTypeIs(contentType: String) = {
      response.header("Content-Type") shouldEqual Some(contentType)
      this
    }

    def body(myQuery: JsValue => JsValue) = {
      new BodyAssertions(myQuery(response.json), this)
    }

    class BodyAssertions(content: JsValue, assertions: Assertions) {
      def is(value: String) = {
        content.as[String] shouldBe value
        assertions
      }
    }

  }

  class HttpVerbs {
    var addAcceptHeader = true
    var method = ""
    var url = ""
    var body :Option[JsValue] = None
    var hc = HeaderCarrier()

    def get(path: String) = {
      assert(path.startsWith("/"), "please provide only a path starting with '/'")
      this.url = s"http://localhost:$port$path"
      this.method = "GET"
      this
    }

    def delete(path: String) = {
      assert(path.startsWith("/"), "please provide only a path starting with '/'")
      this.url = s"http://localhost:$port$path"
      this.method = "DELETE"
      this
    }

    def post(path: String, body: Option[JsValue] = None) = {
      assert(path.startsWith("/"), "please provide only a path starting with '/'")
      this.url = s"http://localhost:$port$path"
      this.method = "POST"
      this.body = body
      this
    }

    def put(path: String, body: Option[JsValue]) = {
      assert(path.startsWith("/"), "please provide only a path starting with '/'")
      this.url = s"http://localhost:$port$path"
      this.method = "PUT"
      this.body = body
      this
    }

    private def withHeader(name : String, value: String) = {
      hc.withExtraHeaders((name, value))
      this
    }

    def withoutAcceptHeader() = {
      this.addAcceptHeader = false
      this
    }

    def thenAssertThat() = {
      if (addAcceptHeader) hc = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.1.0+json"))

      withClue(s"Request $method $url") {
        method match {
          case "GET" => new Assertions(Http.get(url)(hc))
          case "DELETE" => new Assertions(Http.delete(url)(hc))
          case "POST" => {
            body match {
              case Some(jsonBody) => new Assertions(Http.postJson(url, jsonBody)(hc))
              case None => new Assertions(Http.postEmpty(url)(hc))
            }
          }
          case "PUT" => {
            val jsonBody = body.getOrElse(throw new RuntimeException("Body for PUT must be provided"))
            new Assertions((Http.putJson(url, jsonBody)(hc)))
          }
        }
      }
    }

    def withAcceptHeader() = {
      addAcceptHeader = true
      this
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

      stubFor(get(urlPathEqualTo(s"/auth/authority")).willReturn(aResponse().withBody(json).withStatus(200).withHeader("Content-Type", "application/json")))
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

      stubFor(get(urlPathEqualTo(s"/auth/authority")).willReturn(aResponse().withBody(json).withStatus(200).withHeader("Content-Type", "application/json")))
      this
    }


  }

  def given() = new Givens()

}


