package org.scahal.classifier

import org.specs2.Specification

/**
 * Created by IntelliJ IDEA.
 * User: wfaler
 * Date: 26/03/2012
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 */

class FeatureMatrixSpec extends Specification{ def is =

  "The FeatureMatrix should" ^
  p^
    "Add columns and rows as expected" ! happyPath^
    "Throw an Exception if row column is not of expected type" ! wrongColumn^
    "Train an outcomes model correctly" ! trainModelWithOutcomes^
    "summarize all categorical features of a matrix correctly" ! featuresInMatrix^
    "summarize all categorical features of a model correctly" ! allFeatures^
    "create a laplace smoothed map correctly" ! laplaceSmoothing^
    end
  
  def happyPath = {
    val matrix = FeatureMatrix(List(CategoricalFeature("1", "hello"), CategoricalFeature("2", "world")))
    val result = matrix(List(CategoricalFeature("1", "howdy"), CategoricalFeature("3", "person")))

    (result.columns must have size(3)) and
    (result.rows must have size(2)) and
    (result.columns must contain(FeatureColumn("1", classOf[CategoricalFeature[String]]))) and
    (result.columns must contain(FeatureColumn("2", classOf[CategoricalFeature[String]]))) and
    (result.columns must contain(FeatureColumn("3", classOf[CategoricalFeature[String]]))) and
    (result.rows must contain(List(CategoricalFeature("1", "howdy"), CategoricalFeature("3", "person")))) and
    (result.rows must contain(List(CategoricalFeature("1", "hello"), CategoricalFeature("2", "world")))) //and
  }
  
  def wrongColumn = {
    val matrix = FeatureMatrix(List(CategoricalFeature("1", "hello"), CategoricalFeature("2", "world")))
    matrix(List(ContinuousFeature("1", 23), CategoricalFeature("3", "person"))) must throwAn[IllegalStateException]
  }
  
  def trainModelWithOutcomes = {
    val input = List(Event("success", List(CategoricalFeature("1", "hello"), CategoricalFeature("2", "world"))),
        Event("success", List(CategoricalFeature("1", "howdy"), CategoricalFeature("2", "planet"))),
      Event("failure", List(CategoricalFeature("1", "bye"), CategoricalFeature("2", "venus"))),
              Event("failure", List(CategoricalFeature("1", "evening"), CategoricalFeature("2", "mars"))))
    val outcomes = ModelBuilder(input)
    (outcomes.contains("success") must  beTrue) and
    (outcomes.contains("failure") must  beTrue) and
    (outcomes("success").rows must have size(2)) and
    (outcomes("failure").rows must have size(2)) and
    (outcomes("failure").rows must contain(input(2).features)) and
    (outcomes("failure").rows must contain(input(3).features)) and
    (outcomes("success").rows must contain(input(0).features)) and
    (outcomes("success").rows must contain(input(1).features))
  }

  def featuresInMatrix = {
    val matrix = FeatureMatrix(List(CategoricalFeature("1", "hello"), CategoricalFeature("2", "world")))
    val allFeatures = matrix(List(CategoricalFeature("1", "howdy"), CategoricalFeature("3", "person"))).categoricalFeatures()

    (allFeatures(CategoricalFeature("1", "hello").featureColumn) must have size(2)) and
    (allFeatures(CategoricalFeature("1", "hello").featureColumn) must have contain(CategoricalFeature("1", "hello"))) and
    (allFeatures(CategoricalFeature("1", "hello").featureColumn) must have contain(CategoricalFeature("1", "howdy"))) and
    (allFeatures(CategoricalFeature("2", "hello").featureColumn) must have size(1)) and
    (allFeatures(CategoricalFeature("2", "hello").featureColumn) must have contain(CategoricalFeature("2", "world"))) and
    (allFeatures(CategoricalFeature("3", "hello").featureColumn) must have size(1)) and
    (allFeatures(CategoricalFeature("3", "hello").featureColumn) must have contain(CategoricalFeature("3", "person")))
  }

  def allFeatures = {
    val matrix = FeatureMatrix(List(CategoricalFeature("1", "hello"), CategoricalFeature("2", "world"))).
      apply(List(CategoricalFeature("1", "howdy"), CategoricalFeature("3", "person")))

    val matrix2 = FeatureMatrix(List(CategoricalFeature("1", "hello"), CategoricalFeature("2", "planet"))).
      apply(List(CategoricalFeature("1", "howdy"), CategoricalFeature("4", "ogre")))
    val model = Map("success" -> matrix, "failure" -> matrix2)

    val allFeatures = AllFeatures(model)

    (allFeatures(CategoricalFeature("1", "hello").featureColumn) must have size(2)) and
     (allFeatures(CategoricalFeature("1", "hello").featureColumn) must have contain(CategoricalFeature("1", "hello"))) and
     (allFeatures(CategoricalFeature("1", "hello").featureColumn) must have contain(CategoricalFeature("1", "howdy"))) and
     (allFeatures(CategoricalFeature("2", "hello").featureColumn) must have size(2)) and
     (allFeatures(CategoricalFeature("2", "hello").featureColumn) must have contain(CategoricalFeature("2", "world"))) and
     (allFeatures(CategoricalFeature("2", "hello").featureColumn) must have contain(CategoricalFeature("2", "planet"))) and
     (allFeatures(CategoricalFeature("3", "hello").featureColumn) must have size(1)) and
     (allFeatures(CategoricalFeature("3", "hello").featureColumn) must have contain(CategoricalFeature("3", "person"))) and
     (allFeatures(CategoricalFeature("4", "hello").featureColumn) must have size(1)) and
     (allFeatures(CategoricalFeature("4", "hello").featureColumn) must have contain(CategoricalFeature("4", "ogre")))
  }

  def laplaceSmoothing = {
    val matrix = FeatureMatrix(List(CategoricalFeature("1", "hello"), CategoricalFeature("2", "world"))).
      apply(List(CategoricalFeature("1", "howdy"), CategoricalFeature("3", "person")))

    val matrix2 = FeatureMatrix(List(CategoricalFeature("1", "hello"), CategoricalFeature("2", "planet"))).
      apply(List(CategoricalFeature("1", "howdy"), CategoricalFeature("4", "ogre")))
    val model = Map("success" -> matrix, "failure" -> matrix2)

    val smap  = LaplaceInitialMap(model)
    val succ = smap("success")
    val fail = smap("failure")

    (succ.keySet must have size(4)) and
    (fail.keySet must have size(4)) and
    (succ(FeatureColumn("4", classOf[CategoricalFeature[_]])).get(CategoricalFeature("4", "ogre")).get must be_==(1)) and
    (succ(FeatureColumn("1", classOf[CategoricalFeature[_]])).get(CategoricalFeature("1", "hello")).get must be_==(1)) and
    (succ(FeatureColumn("1", classOf[CategoricalFeature[_]])).get(CategoricalFeature("1", "howdy")).get must be_==(1)) and
    (succ(FeatureColumn("2", classOf[CategoricalFeature[_]])).get(CategoricalFeature("2", "world")).get must be_==(1)) and
    (succ(FeatureColumn("2", classOf[CategoricalFeature[_]])).get(CategoricalFeature("2", "planet")).get must be_==(1))
  }

}
