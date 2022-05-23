package com.utep.ilink.swim.models;

public class Data{
    String id;
    int generatedFromDepth;
    Model generatedFromModel;

    public String toString(){
        return id;
    }

    public String getId(){
        return id;
    }

    public Data(String id, int depth){
        this.id = id;
        this.generatedFromDepth = depth;
        this.generatedFromModel = null;
    }

    public void setDepth(int depth){
        this.generatedFromDepth = depth;
    }

    public int generatedFromDepth(){
        return generatedFromDepth;
    }

    public void setModel(Model model){
        this.generatedFromModel = model;
    }

    public Model generatedFromModel(){
        return generatedFromModel;
    }
}