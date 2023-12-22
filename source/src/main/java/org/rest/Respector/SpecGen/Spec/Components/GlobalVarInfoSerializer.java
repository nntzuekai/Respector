package org.rest.Respector.SpecGen.Spec.Components;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.rest.Respector.StaticVarAssignment.StaticExampleLoc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class GlobalVarInfoSerializer implements JsonSerializer<GlobalVarInfo>{
  public JsonElement serialize(GlobalVarInfo src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObj = new JsonObject();

    jsonObj.addProperty("name", src.name);
    jsonObj.addProperty("id", String.format("g%d", src.id));
    jsonObj.addProperty("defining-class", src.location);

    if(src.locs!=null){
      JsonArray locArray=new JsonArray();

      for(StaticExampleLoc eg: src.locs){
        // locArray.add(String.format("%s @ %s:%d", eg.rhs.toString(), eg.file, eg.lineNumber));
        locArray.add(String.format("line %d, %s", eg.lineNumber, eg.file));
      }

      jsonObj.add("locations-of-static-assignments", locArray);
    }
  
    return jsonObj;
  }
}
