package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.Constraints;

import java.lang.reflect.Type;
import java.util.Collections;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ConstraintsObjSerializer implements JsonSerializer<ConstraintsObj>{
  public JsonElement serialize(ConstraintsObj src, Type typeOfSrc, JsonSerializationContext context) {
    Collections.sort(src.constraints);

    return context.serialize(src.constraints);
  }
}
