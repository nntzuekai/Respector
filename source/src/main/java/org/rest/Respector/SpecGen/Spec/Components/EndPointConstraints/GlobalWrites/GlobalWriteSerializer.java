package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalWrites;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GlobalWriteSerializer implements JsonSerializer<GlobalWriteObj> {
  public JsonElement serialize(GlobalWriteObj src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObj = new JsonObject();

    jsonObj.addProperty("name", src.varInfo.name);
    // jsonObj.addProperty("id", src.varInfo.id);
    // jsonObj.addProperty("location", src.varInfo.location);

    JsonElement valuesJsonElement=context.serialize(src.values);
    jsonObj.add("assigned-values", valuesJsonElement);

    JsonElement refJsonElement=context.serialize(src.ref);
    jsonObj.add("location-details", refJsonElement);

    return jsonObj;
  }
}
