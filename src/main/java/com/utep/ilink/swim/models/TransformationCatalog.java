package com.utep.ilink.swim.models;
import java.util.*;

public class TransformationCatalog{
    List<Transformation> transformations;

    public List<Transformation> getTransformationServices(){
        return transformations;
    }

    public String toString(){
        return Arrays.toString(transformations.toArray());
    }
}