package com.utep.ilink.swim.models;

import java.util.*;

public class Transformation extends Computation{
  public Transformation(
    ArrayList<Data> inputs,
    ArrayList<Data> outputs,
    String transformationId,
    ComputationInfo serviceInfo,
    HashSet<String> prerequisites
  ) {
    this.inputs = inputs;
    this.outputs = outputs;
    this.id = transformationId;
    this.computationInfo = serviceInfo;
    this.prerequisites = prerequisites;
  }
}
