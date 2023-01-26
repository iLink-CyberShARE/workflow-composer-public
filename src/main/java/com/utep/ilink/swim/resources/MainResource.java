package com.utep.ilink.swim.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.codahale.metrics.annotation.Timed;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.utep.ilink.swim.models.Computation;
import com.utep.ilink.swim.models.Data;
import com.utep.ilink.swim.models.Model;
import com.utep.ilink.swim.models.ModelCatalog;
import com.utep.ilink.swim.models.Request;
import com.utep.ilink.swim.models.Transformation;
import com.utep.ilink.swim.models.TransformationCatalog;
import com.utep.ilink.swim.util.DataAdapter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/compose")
@Api(value = "/main", description = "Main entry point")
@Produces({ "application/json" })
public class MainResource {

  String APPNAME = "[workflow-composition]:\t";
  String TRANSFORMATIONCOMPONENT_PREFIX_DEFAULT = "Transformation_Component";

  @POST
  @Timed
  @ApiOperation(value = "Workflow composition endpoint", notes = "Returns a workflow plan serialized in JSON format")
  public String compose(
      @FormParam("request") String requestLoc,
      @FormParam("modelcatalog") String modelCatalogLoc) {
    if (requestLoc == null ||
        requestLoc.isEmpty() ||
        modelCatalogLoc == null ||
        modelCatalogLoc.isEmpty()) {
      String error = "{\"msg\": \"Missing user request or model catalog\"}";
      System.out.println(APPNAME + error);
      return error;
    }

    System.out.println(APPNAME + "Initializing workflow composition");
    System.out.println(APPNAME + "Reading request");
    Request request = null;
    Gson gson = new Gson();
    request = gson.fromJson(requestLoc, Request.class);
    System.out.println(request);

    System.out.println(
        APPNAME +
            "Identifying inputs and outputs from request --------------------");
    // Parse inputs from request
    HashSet<Data> collectedData = getInputs(request);
    if (collectedData == null)
      return "{\"msg\": \"Error\"}";
    // Parse outputs from request
    HashSet<Data> target = getOutputs(request);
    if (target == null)
      return "{\"msg\": \"Error\"}";

    // Parse model catalog location
    System.out.println(APPNAME + "Reading model catalog");
    ModelCatalog modelCatalog = null;
    modelCatalog = gson.fromJson(modelCatalogLoc, ModelCatalog.class);
    System.out.println(
        APPNAME + "Loaded models into memory: " + modelCatalog.getModels().size());

    // Parse transformation catalog location
    System.out.println(APPNAME + "Reading transformation services catalog");
    TransformationCatalog transformationCatalog = null;
    transformationCatalog = gson.fromJson(modelCatalogLoc, TransformationCatalog.class);
    if (transformationCatalog != null &&
        transformationCatalog.getTransformationServices() != null)
      System.out.println(
          APPNAME +
              "Loaded transformation services into memory: " +
              transformationCatalog.getTransformationServices().size());

    // Declare a visited set to keep a record of models that have been checked
    HashSet<String> visitedModels = new HashSet<>();

    // Set of models that can be executed based on input
    ArrayList<Model> executableModels = getExecutableModels(
        collectedData,
        visitedModels,
        modelCatalog);

    int depth = 0;
    HashMap<Integer, ArrayList<Computation>> workflowHashMap = new HashMap<>();

    System.out.println(APPNAME + "Computing workflow --------------------");

    System.out.println(APPNAME + "Input available: ");
    System.out.println(Arrays.toString(collectedData.toArray()));

    System.out.println(APPNAME + "Output to compute: ");
    System.out.println(Arrays.toString(target.toArray()));

    System.out.println(APPNAME + "Initial scientific models available: ");
    System.out.println(Arrays.toString(executableModels.toArray()));

    while (executableModels != null && !executableModels.isEmpty()) {
      depth += 2;
      System.out.println(APPNAME + "Starting depth " + depth);
      while (!executableModels.isEmpty()) {
        Model model = executableModels.get(0);
        executableModels.remove(model);
        visitedModels.add(model.getId());

        for (Data output : model.getOutputs()) {
          if (!collectedDataContains(collectedData, output.getId())) {
            output.setDepth(depth);
            output.setModel(model);
            collectedData.add(output);
          }
          removeFromTarget(target, output);
        }
      } // end of while executableModels
      System.out.println(APPNAME + "depth " + depth + " completed, results:");
      System.out.println(APPNAME + "Input collected so far: ");
      System.out.println(Arrays.toString(collectedData.toArray()));
      System.out.println(APPNAME + "Output to be collected: ");
      System.out.println(Arrays.toString(target.toArray()));
      if (!target.isEmpty()) {
        executableModels = getExecutableModels(collectedData, visitedModels, modelCatalog);
        System.out.println(APPNAME + "Next models to be analyzed: ");
        System.out.println(Arrays.toString(executableModels.toArray()));
      } // end of if to check if we collected all target data
    } // end of main while
    if (target.isEmpty()) { // If we collected all target data
      System.out.println(
          APPNAME +
              "Desired output can been collected with the given input/models");
      System.out.println(APPNAME + "Building workflow as HashTable");
      target = getOutputs(request);
      for (Data output : target) {
        createWorkflow(output, collectedData, workflowHashMap);
      }
      System.out.println(APPNAME + "Building workflow as HashTable [DONE]");
      System.out.println(
          APPNAME + "Identifying dependencies for every model [START] ");
      for (int key : workflowHashMap.keySet()) {
        for (Computation model : workflowHashMap.get(key)) {
          HashSet<String> prerequisites = new HashSet<>();
          for (Data input : model.getInputs()) {
            Data data = getDataFrom(collectedData, input.getId());
            if (data != null && data.generatedFromModel() != null) {
              prerequisites.add(data.generatedFromModel().getId()); // Not the initial input data
            }
          }
          model.setPrerequisites(prerequisites);
        }
      }
      System.out.println(
          APPNAME + "Identifying dependencies for every model [DONE] ");
      if (transformationCatalog != null &&
          transformationCatalog.getTransformationServices() != null &&
          transformationCatalog.getTransformationServices().size() > 0) {
        System.out.println(
            APPNAME + "Adding a transformation process for every model [START] ");
        // Odd depth or key in hashmap is a transformation process
        System.out.println(
            APPNAME + "Creating a transformation layer to workflow (no process)");
        for (int i = workflowHashMap.size() * 2; i > 0; i--) {
          workflowHashMap.putIfAbsent(i, new ArrayList<Computation>());
        }
        System.out.println(
            APPNAME +
                "Creating a transformation layer to workflow (no process) - completed");
        int transformationProcessCounter = 0;
        for (int key : workflowHashMap.keySet()) {
          if (key % 2 == 0) { // Depth or key even means a model, odd means transformation service
            for (Computation model : workflowHashMap.get(key)) {
              if (!model
                  .getId()
                  .startsWith(
                      getTransformationProcessIdPrefix(transformationCatalog))) {
                HashSet<String> prerequisites = new HashSet<>();
                for (String prerequisite : model.getPrerequisites()) {
                  prerequisites.add(prerequisite);
                }
                Transformation transformationProcess = getTransformationProcess(transformationCatalog);
                // transformationProcess.setId(
                // transformationProcess.getId() + transformationProcessCounter);
                transformationProcess.setId(model.getId() + "_transformation");

                transformationProcess.setPrerequisites(prerequisites);
                model.getPrerequisites().add(transformationProcess.getId());
                workflowHashMap.get(key - 1).add(transformationProcess);
                transformationProcessCounter++;
              } // end if
            }
          }
        }
        System.out.println(
            APPNAME + "Adding a transformation process for every model [DONE] ");
        System.out.println(
            APPNAME +
                "Adding a transformation layer and process and the end of the workflow [START]");
        System.out
            .println(APPNAME + "Adding a transformation layer and process and the end of the workflow [CANCELED]");
        // This action has been canceled due to a new component: swim-merge
        // // Get models last layer
        // // create hashset of string with ids from every model
        // // create transformation process, set prerequisites to hashset
        // // add transformation process to a new and last layer
        // HashSet<String> prerequisitesLastLayer = new HashSet<>();
        // for (Computation computation : workflowHashMap.get(
        // workflowHashMap.size()))
        // prerequisitesLastLayer.add(computation.getId());
        // Transformation transformationProcessLastLayer = getTransformationProcess(
        // transformationCatalog);
        // // transformationProcessLastLayer.setId(
        // // transformationProcessLastLayer.getId() + transformationProcessCounter);
        // transformationProcessLastLayer.setId("_transformation");
        // transformationProcessCounter++;
        // transformationProcessLastLayer.setPrerequisites(prerequisitesLastLayer);
        // workflowHashMap.putIfAbsent(
        // workflowHashMap.size() + 1,
        // new ArrayList<Computation>());
        // workflowHashMap
        // .get(workflowHashMap.size())
        // .add(transformationProcessLastLayer);

        // System.out.println(
        // APPNAME +
        // "Adding a transformation layer and process and the end of the workflow [DONE]
        // ");
      }

      for (int key : workflowHashMap.keySet()) {
        System.out.print("\nDepth = " + key);
        System.out.print(
            " Number of models to execute = " + workflowHashMap.get(key).size());
      }

      Gson gson2 = new GsonBuilder()
          .registerTypeAdapter(Data.class, new DataAdapter())
          .enableComplexMapKeySerialization()
          .create();
      String workflowJSON = gson2.toJson(workflowHashMap);
      System.out.println(
          "\n\n\t\t----------" +
              APPNAME +
              "Workflow generation [COMPLETED]----------\n");
      System.out.println(workflowJSON);
      return workflowJSON;
    } // end of if to check if we collected all target data
    else {
      System.out.println(
          APPNAME +
              "There are not enough scientific models to generate the desired output");
      return "{\"Message\":\"There are not enough scientific models to generate the desired output\"}";
    }
  }

  /**
   * Returns the prefix used in the transformation services
   */
  public String getTransformationProcessIdPrefix(
      TransformationCatalog transformationCatalog) {
    Transformation transformationComponent = getTransformationProcess(
        transformationCatalog);
    if (transformationComponent == null)
      return TRANSFORMATIONCOMPONENT_PREFIX_DEFAULT;
    return transformationComponent.getId();
  }

  /**
   * TODO: Define an ad-hoc criteria for selecting available transformation
   * services
   * By default, returns first occurrence
   */
  public Transformation getTransformationProcess(
      TransformationCatalog transformationCatalog) {
    if (transformationCatalog == null ||
        transformationCatalog.getTransformationServices().size() == 0)
      return null;
    Transformation transformationMeta = transformationCatalog
        .getTransformationServices()
        .get(0);

    Transformation transformation = new Transformation(
        null,
        null,
        transformationMeta.getId(),
        transformationMeta.getComputationInfo(),
        transformationMeta.getPrerequisites());

    return transformation;
  }

  /**
   * Given a set of data, return the data object that matches a given String id
   * 
   * @param dataId        data id to be collected
   * @param collectedData a set of data collected in the graph\
   */
  private Data getDataFrom(HashSet<Data> collectedData, String dataId) {
    Iterator<Data> dataIterator = collectedData.iterator();
    while (dataIterator.hasNext()) {
      Data data = dataIterator.next();
      if (data.getId().equals(dataId))
        return data;
    }
    return null;
  }

  /**
   * Given a graph, serialized as a hashmap, identify a path to collected required
   * data
   * 
   * @param data            data to be collected
   * @param collectedData   a set of data collected in the graph
   * @param workflowHashMap a graph serialized as a list
   */
  private void createWorkflow(
      Data data,
      HashSet<Data> collectedData,
      HashMap<Integer, ArrayList<Computation>> workflowHashMap) {
    Iterator<Data> iterator = collectedData.iterator();
    if (collectedData.isEmpty())
      return;
    Data analyzedData = iterator.next();
    while (iterator.hasNext()) {
      if (equivalentData(data, analyzedData))
        break;
      analyzedData = iterator.next();
    }
    Model model = analyzedData.generatedFromModel();
    int depth = analyzedData.generatedFromDepth();
    if (model == null)
      return;

    if (!workflowHashMap.containsKey(depth) ||
        !workflowHashMapContains(workflowHashMap, model, depth)) {
      // System.out.println()
      workflowHashMap.putIfAbsent(depth, new ArrayList<Computation>());
      workflowHashMap.get(depth).add(model);

      for (Data inputModel : model.getInputs())
        createWorkflow(
            inputModel,
            collectedData,
            workflowHashMap);
    }
  }

  /**
   * Returns true if the workflow contains a given model
   * 
   * @param workflowHashMap a graph serialized as a hash map
   * @param model           a model to compare
   * @param depth           depth to do the search
   * @return true if hash map contains the given model
   */
  private boolean workflowHashMapContains(
      HashMap<Integer, ArrayList<Computation>> workflowHashMap,
      Model model,
      int depth) {
    if (workflowHashMap == null ||
        workflowHashMap.isEmpty() ||
        !workflowHashMap.containsKey(depth) ||
        workflowHashMap.get(depth) == null ||
        workflowHashMap.get(depth).isEmpty())
      return false;

    for (Computation analyzedModel : workflowHashMap.get(depth)) {
      if (equivalentModel(model, analyzedModel))
        return true;
    }
    return false;
  }

  /**
   * Iterates the target set in search for an equivalent element that matches the
   * given 'output'.
   * If the output is found, it is removed form the target set
   * 
   * @param target a set containing the set of elements to search
   * @param output a data found in the workflow and to be removed from the target
   *               set
   */
  private void removeFromTarget(HashSet<Data> target, Data output) {
    Iterator<Data> targetIterator = target.iterator();
    while (targetIterator.hasNext()) {
      Data data = targetIterator.next();
      if (data.getId().equals(output.getId()))
        targetIterator.remove(); // This condition will check all metadata from data
    }
  }

  /**
   * Parses the request object and returns the desired output.
   * If no output is given, it returns null
   * 
   * @param request a Request object after being parsed from JSON
   * @return null if no output is found, a set of a Data object if output is found
   */
  private HashSet<Data> getOutputs(Request request) {
    if (request == null ||
        request.getOutputs() == null ||
        request.getOutputs().isEmpty()) {
      System.out.println(
          APPNAME + "No output was received or couldn't parse request");
      return null;
    }
    System.out.println(
        APPNAME + "Outputs found: " + request.getOutputs().size());
    HashSet<Data> target = new HashSet<>();
    for (Data output : request.getOutputs()) {
      target.add(output);
    }
    return target;
  }

  /**
   * Parses the request object and returns the available input.
   * If no input is given, it returns null
   * 
   * @param request a Request object after being parsed from JSON
   * @return null if no input is found, a set of a Data object if input is found
   */
  private HashSet<Data> getInputs(Request request) {
    if (request == null ||
        request.getInputs() == null ||
        request.getInputs().isEmpty()) {
      System.out.println(
          APPNAME + "No input was received or couldn't parse request");
      return null;
    }
    System.out.println(APPNAME + "Inputs found: " + request.getInputs().size());
    HashSet<Data> collectedData = new HashSet<>();
    for (Data input : request.getInputs()) {
      collectedData.add(input);
    }
    return collectedData;
  }

  /**
   * Retrieve, from a specified location, a set of models that can be executed
   * based on a specific inputs
   * 
   * @param collectedData a set of collected data to be used as input by models
   * @param visitedModels a set of visited models
   * @return a list of models
   */
  private ArrayList<Model> getExecutableModels(
      HashSet<Data> collectedData,
      HashSet<String> visitedModels,
      ModelCatalog modelCatalog) {
    ArrayList<Model> models = new ArrayList<>();
    for (Model model : modelCatalog.getModels()) {
      if (visitedModels.contains(model.getId()))
        continue;
      boolean executed = true;
      for (Data input : model.getInputs()) {
        if (!collectedDataContains(collectedData, input)) {
          executed = false;
          break;
        }
      }
      if (executed)
        models.add(model);
    }

    // if (!visitedModels.contains(model.getId())) models.add(model);
    return models;
  }

  /**
   * Returns true if data collected contains a given data
   * 
   * @param collectedData a set of data
   * @param inputName     a string of the data to look for
   * @return true if set contains the name of data
   */
  private boolean collectedDataContains(
      HashSet<Data> collectedData,
      String inputName) {
    Iterator<Data> iterator = collectedData.iterator();
    while (iterator.hasNext()) {
      if (equivalentData(iterator.next(), inputName))
        return true;
    }
    return false;
  }

  /**
   * Returns true if data collected contains a given data
   * 
   * @param collectedData a set of data
   * @param inputData     data to look for
   * @return true if set contains input data
   */
  private boolean collectedDataContains(
      HashSet<Data> collectedData,
      Data inputData) {
    return collectedDataContains(collectedData, inputData.getId());
  }

  private boolean equivalentData(Data data, String inputName) {
    return data.getId().equals(inputName);
  }

  private boolean equivalentData(Data data, Data data2) {
    return data.getId().equals(data2.getId());
  }

  private boolean equivalentModel(Computation model, Computation model2) {
    return model.getId().equals(model2.getId());
  }
}
/**
 * Entry point.
 * The algorithm is as follows:
 * Generate a workflow for obtaining some target data (output) from having some
 * available data (input).
 * 
 * Expected response: Workflow
 * 
 * Algorithm (bottom-up):
 * 
 * 1) Set collectedData as a set, having as initial values the values from input
 * 
 * For every input, set as depth 0 and model obtained null
 * 
 * 2) Set target as a set, having as initial values the values from output
 * 
 * 3) Set modelsPool as a set, having as initial value all the models from the
 * model catalog (i.e. MongoDB)
 * 
 * 4) Set visited as a set, having as initial value empty set
 * 
 * 5) Collect all models, from modelsPool, that can be executed based on
 * collectedData and have not been visited yet
 * 
 * Set depth as int, initial value 2 (depth 1 will be used for transformation
 * services)
 * 
 * Set workflow as a HashTable, int - set of Data
 * 
 * 6) For every model collected in the previous step do:
 * 
 * Mark model as visited;
 * 
 * Add output of the set to collectedData;
 * 
 * Set as depth the assigned depth and model obtained the model visited
 * 
 * Remove from target data obtained from the model (output);
 * 
 * 7) If target is empty (we can obtain desired output)
 * 
 * For every desired output do:
 * 
 * (recursive function - A)
 * 
 * retrieve what model generated the output and at what depth
 * 
 * if null (start of graph) return
 * 
 * if the hashtable does not contain model, do:
 * 
 * add to hashtable
 * 
 * retrieve all inputs, for every input do (recursive function - A)
 * 
 * Serialize hashtable (workflow) using general vocab
 * 
 * Else if the length of modelsPool and visited is the same, return false (no
 * workflow can be created that generate the desired output from the given
 * input)
 * 
 * Else repeat step 5 (no workflow has been created yet to generate desired
 * output, but more models can be visited)
 * 
 */
