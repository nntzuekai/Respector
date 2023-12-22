package org.rest.Respector.SpecGen.MediaType;

import org.rest.Respector.EndPointRecog.EndPointParamInfo;
import org.rest.Respector.SpecGen.MediaType.Schema.MediaTypeSchemaObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Schema.ParamSchemaObj;

public class MediaTypeObj {
  MediaTypeSchemaObj schema;

  public MediaTypeSchemaObj getSchema() {
    if(schema==null){
      schema=new MediaTypeSchemaObj();
    }
    return schema;
  }

  public ParamSchemaObj createRequestBodyParam(EndPointParamInfo EPPInfo) {
    MediaTypeSchemaObj schema=getSchema();

    return schema.createRequestBodyParam(EPPInfo);
  }

  public ParamSchemaObj createRequestBodyParam(String name, String type) {
    MediaTypeSchemaObj schema=getSchema();

    return schema.createRequestBodyParam(name, type);
  }
}
