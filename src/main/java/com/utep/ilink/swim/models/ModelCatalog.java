package com.utep.ilink.swim.models;
import java.util.*;

public class ModelCatalog{
    List<Model> models;

    public List<Model> getModels(){
        return models;
    }

    public String toString(){
        return Arrays.toString(models.toArray());
    }
}