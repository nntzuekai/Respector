package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class XPathItemObjSerializer implements JsonSerializer<XPathItemObj> {
  public JsonElement serialize(XPathItemObj src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src.xEndPointConstraints);
  }
}
