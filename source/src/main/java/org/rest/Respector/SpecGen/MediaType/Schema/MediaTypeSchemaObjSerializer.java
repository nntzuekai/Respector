package org.rest.Respector.SpecGen.MediaType.Schema;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.rest.Respector.SpecGen.Spec.Components.GlobalVarInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MediaTypeSchemaObjSerializer implements JsonSerializer<MediaTypeSchemaObj>{
  public JsonElement serialize(MediaTypeSchemaObj src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObj = null;

    if(src.properties!=null){
      jsonObj=new JsonObject();

      JsonElement propertiesObj=context.serialize(src.properties);

      jsonObj.add("properties", propertiesObj);

      if(src.properties.size()>0){
        jsonObj.addProperty("maxProperties", src.properties.size());
      }
    }
  
    return jsonObj;
  }
}
