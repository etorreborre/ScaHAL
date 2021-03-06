package org.scahal.classifier.logistic

import org.specs2.Specification
import org.scahal.classifier.{Event, ContinuousFeature}
import io.Source
import scalala.tensor.dense.DenseMatrix

/**
 * Created with IntelliJ IDEA.
 * User: wfaler
 * Date: 17/04/2012
 * Time: 20:41
 * To change this template use File | Settings | File Templates.
 */

class LogisticRegressionSpec extends Specification{ def is =

  "The Logistic Regression spec" ^
  p ^
    "Calculate the correct Gradient Ascent" !gradientAsc ^
    "Classify with increasing accuracy using Gradient Ascent with more iterations" !increasingAccuracy ^
    end

  def gradientAsc = {
    val weights = GradientAscent(features)
    (weights(0) must be_==(4.124143489627892)) and
    (weights(1) must be_==(0.4800732928842445)) and
    (weights(2) must be_==(-0.6168481970344016))
  }

  def increasingAccuracy = {
    (success(GradientAscent(features, maxCycles = 150)) must be_==(84)) and
    (success(GradientAscent(features)) must be_==(91)) and
    (success(GradientAscent(features, maxCycles = 10000)) must be_==(97))
  }

  def success(weights: List[Double]) = features.foldLeft(0d)((sum, event) => {
        if(event.outcome == LogisticRegressionClassifier(event.features.map(_.asInstanceOf[ContinuousFeature]), weights)(0).value) sum + 1
        else sum
      })

  lazy val features = Source.fromInputStream(this.getClass.getResourceAsStream("/gradAscent.txt"), "UTF-8").getLines().map(line => {
              val splits = line.split("\t")
              Event(splits(2).trim.toDouble, List(ContinuousFeature("0", 1d), ContinuousFeature("1",
                splits(0).trim.toDouble), ContinuousFeature("2", splits(1).trim.toDouble)))
            }).toList
}
