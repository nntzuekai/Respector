package org.rest.Respector.SpecGen.Spec.Path.EndPointOp.RequestBody;

import java.util.TreeMap;

import org.rest.Respector.EndPointRecog.EndPointParamInfo;
import org.rest.Respector.EndPointRecog.ParameterAnnotation.paramLoction;
import org.rest.Respector.SpecGen.MediaType.MediaTypeObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Schema.ParamSchemaObj;

public class RequestBodyObj {
  String description;
  Boolean required;
  TreeMap<String, MediaTypeObj> content;

  public TreeMap<String, MediaTypeObj> getContent() {
    if(content==null){
      content=new TreeMap<>();
    }
    return content;
  }

  public ParamSchemaObj createRequestBodyParam(EndPointParamInfo EPPInfo) {
    TreeMap<String, MediaTypeObj> cont=getContent();
    
    MediaTypeObj mediaTypeObj;
    if(EPPInfo.in==paramLoction.body){
      mediaTypeObj=cont.computeIfAbsent("application/json", c -> new MediaTypeObj());
    }
    else{
      assert EPPInfo.in==paramLoction.formData;

      mediaTypeObj=cont.computeIfAbsent("multipart/form-data", c -> new MediaTypeObj());
    }
    

    return mediaTypeObj.createRequestBodyParam(EPPInfo);
  }

  public ParamSchemaObj createRequestBodyParam(String name, String type) {
    TreeMap<String, MediaTypeObj> cont=getContent();

    MediaTypeObj mediaTypeObj=cont.computeIfAbsent("application/json", c -> new MediaTypeObj());

    return mediaTypeObj.createRequestBodyParam(name, type);
  }
}
