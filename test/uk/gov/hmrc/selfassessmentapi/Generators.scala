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

import org.scalacheck._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand

object Generators {

  def amountGen(lower: BigDecimal, upper: BigDecimal): Gen[BigDecimal] =
    for {
      value <- Gen.chooseNum(lower.intValue(), upper.intValue())
    } yield BigDecimal(value)

  val basicTaxBandAmountGen: Gen[BigDecimal] =
    amountGen(TaxBand.BasicTaxBand().lowerBound, TaxBand.BasicTaxBand().upperBound.get)

  val higherTaxBandAmountGen: Gen[BigDecimal] =
    amountGen(TaxBand.HigherTaxBand().lowerBound, TaxBand.HigherTaxBand().upperBound.get)

  val additionalHigherTaxBandAmountGen: Gen[BigDecimal] = amountGen(
    TaxBand.AdditionalHigherTaxBand().lowerBound,
    TaxBand.AdditionalHigherTaxBand().upperBound.getOrElse(BigDecimal(Int.MaxValue)))

}
