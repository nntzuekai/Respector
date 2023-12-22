package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalRefs;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GlobalRefObjSerializer implements JsonSerializer<GlobalRefObj>{
  public JsonElement serialize(GlobalRefObj src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObj = new JsonObject();

    jsonObj.addProperty("name", src.varInfo.name);
    // jsonObj.addProperty("id", src.varInfo.id);
    // jsonObj.addProperty("location", src.varInfo.location);
    
    JsonElement constriantJsonElement=context.serialize(src.constraints);
    jsonObj.add("constraints", constriantJsonElement);

    JsonElement exampleJsonElement=context.serialize(src.examples);
    jsonObj.add("examples", exampleJsonElement);

    JsonElement refJsonElement=context.serialize(src.ref);
    jsonObj.add("location-details", refJsonElement);

    return jsonObj;
  }
}
