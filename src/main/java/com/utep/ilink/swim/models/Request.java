package com.utep.ilink.swim.models;

import java.util.List;

public class Request{
    List<Data> inputs;
    List<Data> outputs;

    public List<Data> getInputs(){
        return inputs;
    }
    public List<Data> getOutputs(){
        return outputs;
    }
}