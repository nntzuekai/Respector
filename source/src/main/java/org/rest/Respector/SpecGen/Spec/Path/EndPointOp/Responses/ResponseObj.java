package org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Responses;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ResponseObj {
  public transient String HTTPStatusCode;

  public String description;

  public JsonObject content;

  public ResponseObj(String hTTPStatusCode, String description, JsonElement schema) {
    this.HTTPStatusCode = hTTPStatusCode;
    this.description = description;

    if(schema!=null){
      this.content = new JsonObject();
      JsonObject mediaType=new JsonObject();
      mediaType.add("schema", schema);
      this.content.add("application/json", mediaType);
    }
  }

  @Deprecated
  public ResponseObj(String hTTPStatusCode, String description) {
    this.HTTPStatusCode = hTTPStatusCode;
    this.description = description;
  }

  public ResponseObj(String hTTPStatusCode) {
    this.HTTPStatusCode = hTTPStatusCode;
    this.description="";
  }
}
