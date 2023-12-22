package org.rest.Respector.SpecGen.Spec.Components;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SpecComponentsObjSerializer implements JsonSerializer<SpecComponentsObj> {
  public JsonElement serialize(SpecComponentsObj src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObj = new JsonObject();

    jsonObj.add("x-endpoint-constraints", context.serialize(src.xEndPointConstraints));
    jsonObj.add("x-endpoint-interdependence", context.serialize(src.interDependencies));

    JsonObject gVarsObj=new JsonObject();

    for (Entry<Integer, GlobalVarInfo> kw : src.globalsWithStaticVarAssign.entrySet()) {
      gVarsObj.add(String.format("g%d", kw.getKey()), context.serialize(kw.getValue()));
    }

    jsonObj.add("x-global-variables-info", gVarsObj);

    return jsonObj;
  }
}
