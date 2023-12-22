package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointMethodConstraint;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalRefs.GlobalRefObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalWrites.GlobalWriteObj;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class XEndPointMethodConstraintObjSerializer implements JsonSerializer<XEndPointMethodConstraintObj>{
  public JsonElement serialize(XEndPointMethodConstraintObj src, Type typeOfSrc, JsonSerializationContext context){
    JsonObject jsonObj = new JsonObject();

    jsonObj.add("valid-path-conditions", context.serialize(src.validPathConstraints));
    jsonObj.add("invalid-path-conditions", context.serialize(src.invalidPathConstraints));

    if(src.globalReads!=null){
      JsonObject gRefsObj=new JsonObject();
      for(Entry<Integer, GlobalRefObj> kw: src.globalReads.entrySet()){
        String gId=String.format("g%d", kw.getKey());
        gRefsObj.add(gId, context.serialize(kw.getValue()));
      }
      jsonObj.add("global-reads", gRefsObj);
    }
    
    if(src.globalWrites!=null){
      JsonObject gWritesObj= new JsonObject();
      for(Entry<Integer, GlobalWriteObj> kw: src.globalWrites.entrySet()){
        String gId=String.format("g%d", kw.getKey());
        gWritesObj.add(gId, context.serialize(kw.getValue()));
      }
      jsonObj.add("global-writes", gWritesObj);
    }
    
    return jsonObj;
  }
}
