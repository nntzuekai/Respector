package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointConstraints;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class XEndPointConstraintsObjSerializer implements JsonSerializer<XEndPointConstraintsObj>{
  public JsonElement serialize(XEndPointConstraintsObj src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObj;
    if(src.methodConstraints!=null){
      jsonObj=context.serialize(src.methodConstraints).getAsJsonObject();
    }
    else{
      jsonObj=new JsonObject();
    }
    

    JsonElement paramConstraintJsonElement=context.serialize(src.parameterConstraints);
    jsonObj.add("x-parameter-constraints", paramConstraintJsonElement);

    return jsonObj;
  } 
}
