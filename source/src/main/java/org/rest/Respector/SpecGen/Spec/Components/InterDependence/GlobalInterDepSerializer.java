package org.rest.Respector.SpecGen.Spec.Components.InterDependence;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GlobalInterDepSerializer implements JsonSerializer<GlobalInterDepObj>{
  public JsonElement serialize(GlobalInterDepObj src, Type typeOfSrc, JsonSerializationContext context){
    JsonObject jsonObject=new JsonObject();

    for(Entry<Integer, GlobalInterDepItemObj> kw: src.interdependency.entrySet()){
      jsonObject.add(String.format("g%d", kw.getKey()), context.serialize(kw.getValue()));
    }

    return jsonObject;
  }
}
