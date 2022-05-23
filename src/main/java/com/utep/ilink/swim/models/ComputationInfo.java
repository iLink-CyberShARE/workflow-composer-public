package com.utep.ilink.swim.models;

public class ComputationInfo {

  String method;
  String contentType;
  String url;

  public String toString(){
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("\tmethod= ");
    strBuilder.append(method);
    strBuilder.append("\tcontentType= ");
    strBuilder.append(contentType);
    strBuilder.append("\turl= ");
    strBuilder.append(url);
    return strBuilder.toString();
  }

  public String getMethod() {
    return method;
  }

  public String getContentType() {
    return contentType;
  }

  public String geturl() {
    return url;
  }
}
