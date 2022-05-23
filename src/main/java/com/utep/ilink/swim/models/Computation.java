package com.utep.ilink.swim.models;

import java.util.*;

public class Computation {

  List<Data> inputs;
  List<Data> outputs;
  String id;
  ComputationInfo computationInfo;
  HashSet<String> prerequisites;

  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("computation info= ");
    strBuilder.append(id);
    strBuilder.append("\tinputs= ");
    if (inputs != null) strBuilder.append(
      Arrays.toString(inputs.toArray())
    ); else strBuilder.append(" no inputs");
    strBuilder.append("\toutputs= ");
    if (outputs != null) strBuilder.append(
      Arrays.toString(outputs.toArray())
    ); else strBuilder.append(" no outputs");
    strBuilder.append("\tinfo= ");
    strBuilder.append(computationInfo);
    return strBuilder.toString();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Data> getInputs() {
    return inputs;
  }

  public List<Data> getOutputs() {
    return outputs;
  }

  public HashSet<String> getPrerequisites() {
    return prerequisites;
  }

  public ComputationInfo getComputationInfo() {
    return computationInfo;
  }
  
  public void setPrerequisites(HashSet<String> prerequisites) {
    this.prerequisites = prerequisites;
  }
}
