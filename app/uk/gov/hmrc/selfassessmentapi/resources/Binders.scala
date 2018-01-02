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

package uk.gov.hmrc.selfassessmentapi.resources

import java.util.Locale

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.mvc.{PathBindable, QueryStringBindable}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.SourceType.SourceType
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.{SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.utils.ObligationQueryParams

import scala.util.{Failure, Success, Try}

object Binders {

  type OptEither[T] = Option[Either[String, T]]

  private val fromToDateRegex = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-]" +
    "(0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]" +
    "02[-](0[1-9]|1[0-9]|2[0-8])))$"

  implicit def ninoBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[Nino] {
    val desNinoRegex = "^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D]?$"

    def unbind(key: String, nino: Nino): String = stringBinder.unbind(key, nino.value)

    def bind(key: String, value: String): Either[String, Nino] = {
      val normalisedNino = value.replaceAll("\\s", "").toUpperCase(Locale.ROOT)

      if (Nino.isValid(normalisedNino) && normalisedNino.matches(desNinoRegex)) {
        Right(Nino(normalisedNino))
      } else {
        Left("ERROR_NINO_INVALID")
      }
    }
  }

  implicit def taxYearBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[TaxYear] {

    def unbind(key: String, taxYear: TaxYear): String = stringBinder.unbind(key, taxYear.value)

    def bind(key: String, value: String): Either[String, TaxYear] = {
      TaxYear.createTaxYear(value) match {
        case Some(taxYear) => Right(taxYear)
        case None => Left("ERROR_TAX_YEAR_INVALID")
      }
    }
  }

  implicit val sourceTypeBinder = new PathBindable[SourceType] {

    def unbind(key: String, `type`: SourceType): String = `type`.toString

    def bind(key: String, value: String): Either[String, SourceType] = {
      SourceType.values.find(sourceType =>  value.equals(sourceType.toString)) match {
        case Some(v) => Right(v)
        case None => Left("ERROR_INVALID_SOURCE_TYPE")
      }
    }
  }

  implicit val propertyTypeBinder = new PathBindable[PropertyType] {

    override def unbind(key: String, value: PropertyType): String = value.toString

    override def bind(key: String, value: String): Either[String, PropertyType] = {
      PropertyType.values.find(propType => value.equals(propType.toString)) match {
        case Some(v) => Right(v)
        case None => Left("ERROR_INVALID_PROPERTY_TYPE")
      }
    }
  }

  val format: String = "yyy-MM-dd"

  implicit val datePathBinder = new PathBindable[LocalDate] {

    override def unbind(key: String, date: LocalDate): String = date.toString

    override def bind(key: String, dateString: String): Either[String, LocalDate] =
      Try{ DateTimeFormat.forPattern(format).parseLocalDate(dateString) } match {
        case Success(v) => Right(v)
        case Failure(_) => Left("ERROR_INVALID_DATE")
      }

  }


  private def matches(value: String, regex: String) =
    if (value.matches(regex)) value else throw new IllegalArgumentException


  def dateQueryFrom(stringBinder: QueryStringBindable[String], params: Map[String, Seq[String]],
                    paramName : String, errorCode : String): OptEither[LocalDate] = {
    stringBinder.bind(paramName, params).map {
      case Right(value) =>
        (for {
          _ <- Try(matches(value, fromToDateRegex))
          res <- Try(Right(new LocalDate(value)))
        } yield res).getOrElse(Left(errorCode))
      case Left(_) => Left(errorCode)
    }
  }


  implicit def obligationQueryParamsBinder(implicit stringBinder: QueryStringBindable[String]) =
    new QueryStringBindable[ObligationQueryParams] {

      def validDateRange(fromOpt: OptEither[LocalDate], toOpt: OptEither[LocalDate]) =
        for {
          from <- fromOpt
          if from.isRight
          to <- toOpt
          if to.isRight
        } yield
          (from.right.get, to.right.get) match {
            case (from, to) if to.isBefore(from) => Left("ERROR_INVALID_DATE_RANGE")
            case _ => Right(()) // object wrapped in Right irrelevant
          }


      override def bind(key: String, params: Map[String, Seq[String]]) : OptEither[ObligationQueryParams] = {

        val from = dateQueryFrom(stringBinder, params, "from", "ERROR_INVALID_DATE_FROM")
        val to = dateQueryFrom(stringBinder, params, "to", "ERROR_INVALID_DATE_TO")

        val errors = for {
          paramOpt <- Seq(from,
            to,
            validDateRange(from, to))
          param <- paramOpt
          if param.isLeft
        } yield param.left.get

        if (errors.isEmpty) {
          Some(Right(ObligationQueryParams(from.map(_.right.get), to.map(_.right.get))))
        } else {
          Some(Left(errors.head))
        }
      }

      override def unbind(key: String, value: ObligationQueryParams): String =
        stringBinder.unbind(key, value.map(key).fold("")(_.toString))
    }
}
