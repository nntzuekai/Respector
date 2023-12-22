package org.rest.Respector.SpecGen.Spec.Path;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PathItemObjSerializer implements JsonSerializer<PathItemObj> {
  public JsonElement serialize(PathItemObj src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src.operations);
  }
}
