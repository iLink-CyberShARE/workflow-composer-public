package com.utep.ilink.swim.models;

import java.util.*;

public class Model extends Computation{
  public Model(
    ArrayList<Data> inputs,
    ArrayList<Data> outputs,
    String modelId,
    ComputationInfo serviceInfo,
    HashSet<String> prerequisites
  ) {
    this.inputs = inputs;
    this.outputs = outputs;
    this.id = modelId;
    this.computationInfo = serviceInfo;
    this.prerequisites = prerequisites;
  }
}
