package org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Schema.Items;

import org.apache.commons.lang3.tuple.Pair;
import org.rest.Respector.SpecGen.Spec.TypeTranslation;

import soot.Type;

public class ParamSchemaItemsObj {
  String type;
  String format;

  public ParamSchemaItemsObj(Type type) {
    Pair<String, String> jsonType = TypeTranslation.typeToJSONTypeAndFormat(type);
    this.type=jsonType.getLeft();
    this.format=jsonType.getRight();

    if(this.type.equals("array")){
      this.type="object";
    }
  }

  public ParamSchemaItemsObj(String type, String format) {
    this.type = type;
    this.format = format;
  }
}
