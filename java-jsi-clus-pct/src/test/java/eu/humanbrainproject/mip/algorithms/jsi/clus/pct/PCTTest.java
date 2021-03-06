
package eu.humanbrainproject.mip.algorithms.jsi.clus.pct;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Maps;
import com.opendatagroup.hadrian.jvmcompiler.PFAEngine;
import com.opendatagroup.hadrian.jvmcompiler.PFAEngine$;

import eu.humanbrainproject.mip.algorithms.jsi.common.ClusAlgorithm;
import eu.humanbrainproject.mip.algorithms.jsi.common.ClusConstants;
import eu.humanbrainproject.mip.algorithms.jsi.common.ClusHelpers;
import eu.humanbrainproject.mip.algorithms.jsi.common.ClusMeta;
import eu.humanbrainproject.mip.algorithms.jsi.dummy.FileInputData;
import eu.humanbrainproject.mip.algorithms.jsi.serializers.pfa.ClusGenericSerializer;
import eu.humanbrainproject.mip.algorithms.jsi.serializers.pfa.ClusModelPFASerializer;
import eu.humanbrainproject.mip.algorithms.jsi.serializers.pfa.ClusVisualizationSerializer;
import scala.Option;
import scala.collection.immutable.HashMap;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.model.test.NodeTest;

import org.apache.avro.data.Json;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** @author Martin Breskvar */
@DisplayName("With CLUS PCT algorithm")
public class PCTTest {

  private static ObjectMapper mapper = new ObjectMapper();

  private FileInputData getData(String[] featureNames, String[] variableNames, String resourceFile)
      throws IOException {

    final URL resource = getClass().getResource(resourceFile);
    assertNotNull(resource);

    return new FileInputData(featureNames, variableNames, resource, ".csv", 0);
  }

  private FileInputData getRegressionData(String[] featureNames, String[] variableNames)
      throws IOException {
    return getData(featureNames, variableNames, "regression.csv");
  }

  private FileInputData getClassificationData(String[] featureNames, String[] variableNames)
      throws IOException {
    return getData(featureNames, variableNames, "classification.csv");
  }

  private ClusAlgorithm<ClusNode> getAlgorithm(FileInputData input) {
    PCTMeta clusMeta = new PCTMeta();

    return getAlgorithm(input, clusMeta);
  }

  private ClusAlgorithm<ClusNode> getAlgorithm(FileInputData input, PCTMeta clusMeta) {

    ClusGenericSerializer<ClusNode> modelSerializer = new PCTSerializer();
    ClusModelPFASerializer<ClusNode> mainSerializer = new ClusModelPFASerializer<>(modelSerializer);
    ClusAlgorithm<ClusNode> algorithm = new ClusAlgorithm<>(input, mainSerializer, clusMeta);

    return algorithm;
  }

  private PFAEngine<Object, Object> getPFAEngine(String pfa) {
    return PFAEngine$.MODULE$
        .fromJson(pfa, new HashMap<>(), "0.8.5", Option.empty(), 1, Option.empty(), false)
        .head();
  }

  private void predictST(
      String pfa,
      String[] featureNames,
      double[][] testData,
      Object[] expected,
      boolean classification)
      throws JsonGenerationException, JsonMappingException, IOException {
    System.out.println("Trying to get PFAEngine...");
    PFAEngine<Object, Object> engine = getPFAEngine(pfa);

    for (int tuple = 0; tuple < testData.length; tuple++) {
      Map<String, Double> inputs = Maps.newHashMap();
      for (int i = 0; i < featureNames.length; i++) {
        inputs.put(featureNames[i], testData[tuple][i]);
      }
      final Object jsonInput = engine.jsonInput(new ObjectMapper().writeValueAsString(inputs));
      final Object jsonOutput = engine.jsonOutput(engine.action(jsonInput));

      System.out.println("Input: " + jsonInput.toString() + " Output: " + jsonOutput.toString());

      if (classification) {
        String result = jsonOutput.toString().replaceAll("\"", "");

        assertEquals(expected[tuple], result);
      } else {
        Double result = Double.parseDouble(jsonOutput.toString());

        assertEquals(expected[tuple], result.doubleValue());
      }
    }
  }

  private void predictMT(String pfa, String[] featureNames, double[][] testData, String[] expected)
      throws JsonGenerationException, JsonMappingException, IOException {
    System.out.println("Trying to get PFAEngine...");
    PFAEngine<Object, Object> engine = getPFAEngine(pfa);

    for (int tuple = 0; tuple < testData.length; tuple++) {
      Map<String, Double> inputs = Maps.newHashMap();
      for (int i = 0; i < featureNames.length; i++) {
        inputs.put(featureNames[i], testData[tuple][i]);
      }
      final Object jsonInput = engine.jsonInput(new ObjectMapper().writeValueAsString(inputs));
      final Object jsonOutput = engine.jsonOutput(engine.action(jsonInput));

      System.out.println("Input: " + jsonInput.toString() + " Output: " + jsonOutput.toString());

      assertEquals(expected[tuple], jsonOutput.toString());
    }
  }

  @Test
  @DisplayName(
      "we can implement a predictive clustering tree for ST regression and export its model to PFA")
  public void testRegressionST() throws Exception {
    String[] featureNames = new String[] {"input1", "input2"};
    String[] variableNames = new String[] {"output1"};

    ClusAlgorithm<ClusNode> algorithm =
        getAlgorithm(getRegressionData(featureNames, variableNames));

    algorithm.run();

    System.out.println(algorithm.toPrettyPFA());
    String pfa = algorithm.toPFA();

    assertTrue(!pfa.contains("error"));
    assertTrue(pfa.contains("\"action\""));

    final JsonNode jsonPfa =
        mapper.readTree(pfa.replaceFirst("SELECT \\* FROM .*\\\\\"", "SELECT"));
    final JsonNode jsonExpected = mapper.readTree(getClass().getResource("regressionST.pfa.json"));

    assertEquals(jsonExpected, jsonPfa);

    double[][] testData =
        new double[][] {
          {1.2, 2.4}, {6.7, 8.9}, {4.6, 23.4}, {7.6, 5.4}, {1.2, 1.6}, {3.4, 4.7}, {3.4, 6.5}
        };
    Double[] expected =
        new Double[] {
          -0.003907983870967769,
          -0.003907983870967769,
          -0.003907983870967769,
          -0.003907983870967769,
          -0.003907983870967769,
          -0.003907983870967769,
          -0.003907983870967769
        };

    predictST(pfa, featureNames, testData, expected, false);
  }

  @Test
  @DisplayName(
      "we can implement a predictive clustering tree for ST classification and export its model to PFA")
  public void testClassificationST() throws Exception {
    String[] featureNames = new String[] {"input1", "input2"};
    String[] variableNames = new String[] {"output1"};

    ClusAlgorithm<ClusNode> algorithm =
        getAlgorithm(getClassificationData(featureNames, variableNames));

    algorithm.run();

    System.out.println(algorithm.toPrettyPFA());
    String pfa = algorithm.toPFA();

    assertTrue(!pfa.contains("error"));
    assertTrue(pfa.contains("\"action\""));

    final JsonNode jsonPfa =
        mapper.readTree(pfa.replaceFirst("SELECT \\* FROM .*\\\\\"", "SELECT"));
    final JsonNode jsonExpected =
        mapper.readTree(getClass().getResource("classificationST.pfa.json"));

    assertEquals(jsonExpected, jsonPfa);

    double[][] testData =
        new double[][] {
          {1.2, 2.4}, {6.7, 8.9}, {4.6, 23.4}, {7.6, 5.4}, {1.2, 1.6}, {3.4, 4.7}, {3.4, 6.5}
        };
    String[] expected = new String[] {"A", "B", "A", "B", "A", "A", "A"};

    predictST(pfa, featureNames, testData, expected, true);
  }

  @Test
  @DisplayName(
      "we can implement a predictive clustering tree for MT regression and export its model to PFA")
  public void testRegressionMT() throws Exception {
    String[] featureNames = new String[] {"input1", "input2"};
    String[] variableNames = new String[] {"output1", "output2"};

    ClusAlgorithm<ClusNode> algorithm =
        getAlgorithm(getRegressionData(featureNames, variableNames));

    algorithm.run();

    System.out.println(algorithm.toPrettyPFA());
    String pfa = algorithm.toPFA();

    assertTrue(!pfa.contains("error"));
    assertTrue(pfa.contains("\"action\""));

    final JsonNode jsonPfa =
        mapper.readTree(pfa.replaceFirst("SELECT \\* FROM .*\\\\\"", "SELECT"));
    final JsonNode jsonExpected = mapper.readTree(getClass().getResource("regressionMT.pfa.json"));

    assertEquals(jsonExpected, jsonPfa);

    double[][] testData =
        new double[][] {
          {1.2, 2.4}, {6.7, 8.9}, {4.6, 23.4}, {7.6, 5.4}, {1.2, 1.6}, {3.4, 4.7}, {3.4, 6.5}
        };

    String[] expected =
        new String[] {
          "{\"output1\":-0.003907983870967769,\"output2\":0.0031527595307917914}",
          "{\"output1\":-0.003907983870967769,\"output2\":0.0031527595307917914}",
          "{\"output1\":-0.003907983870967769,\"output2\":0.0031527595307917914}",
          "{\"output1\":-0.003907983870967769,\"output2\":0.0031527595307917914}",
          "{\"output1\":-0.003907983870967769,\"output2\":0.0031527595307917914}",
          "{\"output1\":-0.003907983870967769,\"output2\":0.0031527595307917914}",
          "{\"output1\":-0.003907983870967769,\"output2\":0.0031527595307917914}"
        };

    predictMT(pfa, featureNames, testData, expected);
  }

  @Test
  @DisplayName(
      "we can implement a predictive clustering tree for MT classification and export its model to PFA")
  public void testClassificationMT() throws Exception {
    String[] featureNames = new String[] {"input1", "input2"};
    String[] variableNames = new String[] {"output1", "output2"};

    ClusAlgorithm<ClusNode> algorithm =
        getAlgorithm(getClassificationData(featureNames, variableNames));

    algorithm.run();

    System.out.println(algorithm.toPrettyPFA());
    String pfa = algorithm.toPFA();

    assertTrue(!pfa.contains("error"));
    assertTrue(pfa.contains("\"action\""));

    final JsonNode jsonPfa =
        mapper.readTree(pfa.replaceFirst("SELECT \\* FROM .*\\\\\"", "SELECT"));
    final JsonNode jsonExpected =
        mapper.readTree(getClass().getResource("classificationMT.pfa.json"));

    assertEquals(jsonExpected, jsonPfa);

    double[][] testData =
        new double[][] {
          {1.2, 2.4}, {6.7, 8.9}, {4.6, 23.4}, {7.6, 5.4}, {1.2, 1.6}, {3.4, 4.7}, {3.4, 6.5}
        };

    String[] expected =
        new String[] {
          "{\"output1\":\"A\",\"output2\":\"X\"}",
          "{\"output1\":\"B\",\"output2\":\"Y\"}",
          "{\"output1\":\"A\",\"output2\":\"X\"}",
          "{\"output1\":\"B\",\"output2\":\"Y\"}",
          "{\"output1\":\"A\",\"output2\":\"X\"}",
          "{\"output1\":\"A\",\"output2\":\"X\"}",
          "{\"output1\":\"A\",\"output2\":\"X\"}"
        };

    predictMT(pfa, featureNames, testData, expected);
  }

  private void commonVisJSAssertions(ClusNode model, String vis, String[] featureNames) {
    assertNotNull(vis);
    assertTrue(vis.contains("var nodes=[]; var edges=[];"));
    assertTrue(vis.contains("nodes.push"));
    assertTrue(vis.contains("new vis.Network("));

    // first node
    if (!model.atBottomLevel()) {
      NodeTest t = model.getTest();
      String testString = t.getString();

      boolean known = false;
      for (String a : featureNames) {
        if (testString.contains(a)) {
          known = true;
          break;
        }
      }
      assertTrue(known);

      assertTrue(vis.contains("edges.push"));
    } else {
      String prediction = model.getTargetStat().toString();
      assertTrue(prediction.length() > 0);
    }
  }

  @Test
  @DisplayName("we can implement a predictive clustering tree for ST regression and visualize it")
  public void testVisualizationST() throws Exception {
    String[] featureNames = new String[] {"input1", "input2"};
    String[] variableNames = new String[] {"output1"};

    PCTMeta meta = new PCTMeta();
    meta.SETTINGS.remove("[Tree]");
    meta.WHICH_MODEL_TO_USE = 1;
    meta.SETTINGS.get("[Model]").put("MinimalWeight", "30");

    ClusAlgorithm<ClusNode> algorithm =
        getAlgorithm(getRegressionData(featureNames, variableNames), meta);

    algorithm.run();

    ClusNode model = algorithm.getModel();
    assertNotNull(model);

    PCTVisualizer visualizer = new PCTVisualizer();

    String vis = visualizer.getVisualizationString(model, variableNames);

    System.out.println(vis);

    commonVisJSAssertions(model, vis, featureNames);
  }

  @Test
  @DisplayName("we can implement a predictive clustering tree for MT regression and visualize it")
  public void testVisualizationMT() throws Exception {
    String[] featureNames = new String[] {"input1", "input2"};
    String[] variableNames = new String[] {"output1", "output2"};

    PCTMeta meta = new PCTMeta();
    meta.SETTINGS.remove("[Tree]");
    meta.WHICH_MODEL_TO_USE = 1;
    meta.SETTINGS.get("[Model]").put("MinimalWeight", "30");

    ClusAlgorithm<ClusNode> algorithm =
        getAlgorithm(getRegressionData(featureNames, variableNames), meta);

    algorithm.run();

    ClusNode model = algorithm.getModel();
    assertNotNull(model);

    PCTVisualizer visualizer = new PCTVisualizer();

    String vis = visualizer.getVisualizationString(model, variableNames);

    System.out.println(vis);

    commonVisJSAssertions(model, vis, featureNames);
  }

  @AfterEach
  public void cleanUp() {
    ClusHelpers.CleanUp();
  }
}
