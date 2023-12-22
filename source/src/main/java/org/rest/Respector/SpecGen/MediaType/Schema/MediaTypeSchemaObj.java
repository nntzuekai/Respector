package org.rest.Respector.SpecGen.MediaType.Schema;

import java.util.TreeMap;

import org.rest.Respector.EndPointRecog.EndPointParamInfo;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Schema.ParamSchemaObj;

public class MediaTypeSchemaObj {
  TreeMap<String, ParamSchemaObj> properties;

  public TreeMap<String, ParamSchemaObj> getProperties() {
    if(properties==null){
      properties=new TreeMap<>();
    }
    return properties;
  }

  public ParamSchemaObj createRequestBodyParam(EndPointParamInfo EPPInfo) {
    TreeMap<String, ParamSchemaObj> properties=getProperties();

    ParamSchemaObj paramSchema=properties.computeIfAbsent(EPPInfo.name, c -> new ParamSchemaObj(EPPInfo.type, null));

    return paramSchema;
  }

  public ParamSchemaObj createRequestBodyParam(String name, String type) {
    TreeMap<String, ParamSchemaObj> properties=getProperties();

    ParamSchemaObj paramSchema=properties.computeIfAbsent(name, c -> new ParamSchemaObj(type, null));

    return paramSchema;
  }
}
