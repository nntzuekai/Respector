package org.rest.Respector.EndPointRecog;

import org.rest.Respector.EndPointRecog.ParameterAnnotation.paramLoction;

import soot.Type;

public class EndPointParamInfo {
  public String name;
  public int index;
  public Boolean required;
  public paramLoction in;
  public String defaultValue;
  public Type type;

  public EndPointParamInfo(String name, int index, Boolean required, paramLoction in, String defaultValue, Type type) {
    this.name = name;
    this.index = index;
    this.required = required;
    this.in = in;
    this.defaultValue = defaultValue;
    this.type=type;
  }
}
