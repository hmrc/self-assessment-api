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

package uk.gov.hmrc.selfassessmentapi

import com.mongodb.BasicDBObject
import com.mongodb.casbah.MongoClient
import de.flapdoodle.embed.mongo.config.{MongodConfigBuilder, Net, RuntimeConfigBuilder}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{Command, MongodExecutable, MongodProcess, MongodStarter}
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.runtime.Network
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.Logger
import uk.gov.hmrc.mongo.MongoConnector

import scala.util.Try

trait MongoEmbeddedDatabase extends UnitSpec with BeforeAndAfterAll with BeforeAndAfterEach {

  private var mongodExe: MongodExecutable = _
  private var mongod: MongodProcess = _

  private val diskPort = 27017
  private val embeddedPort = 12345
  private val localhost = "127.0.0.1"
  private val mongoUri = sys.env.getOrElse("MONGO_TEST_URI", s"mongodb://$localhost:$embeddedPort/self-assessment-api")
  private lazy val useEmbeddedMongo = mongoUri.contains(embeddedPort.toString)
  lazy val runtimeConfig = new RuntimeConfigBuilder()
    .defaults(Command.MongoD)
    .processOutput(ProcessOutput.getDefaultInstanceSilent())
    .build()

  implicit val mongo = new MongoConnector(mongoUri).db

  lazy protected val mongoClient = MongoClient("localhost", if (useEmbeddedMongo) embeddedPort else diskPort)("self-assessment-api")


  protected def startEmbeddedMongo() = {
    if (useEmbeddedMongo) {
      Logger.info("Starting embedded mongo")
      mongodExe = MongodStarter.getInstance(runtimeConfig).prepare(new MongodConfigBuilder()
        .version(Version.Main.PRODUCTION)
        .net(new Net(localhost, embeddedPort, Network.localhostIsIPv6()))
        .build())
      mongod = mongodExe.start()
    }
  }

  protected def stopEmbeddedMongo() = {
    Try {
      if (useEmbeddedMongo) {
        mongod.stop()
        mongodExe.stop()
      }
    } recover {
      case ex: Throwable => Logger.info(s"MONGO_STOP_FAILED: Couldn't kill mongod process! : $ex")
    }
  }

  override def beforeAll() = {
    if (mongod != null && mongod.isProcessRunning) stopEmbeddedMongo()
    startEmbeddedMongo()
  }

  override def afterAll() = {
    stopEmbeddedMongo()
  }

  override def beforeEach() = {
    clearMongoCollections()
  }

  protected def clearMongoCollections() = {
    List("selfEmployments", "employments", "selfAssessments", "jobHistory", "liabilities").foreach {
      coll => mongoClient.getCollection(coll).remove(new BasicDBObject())
    }
  }
}