package org.rest.Respector.SpecGen.Spec;

import com.google.gson.annotations.SerializedName;

public class ReferenceObj {
  @SerializedName("$ref")
  public String ref;

  public transient Object referree;

  public ReferenceObj(String ref, Object referree) {
    this.ref = ref;
    this.referree = referree;
  }

  public ReferenceObj(String ref) {
    this.ref = ref;
  }

  public ReferenceObj() {
  }
}
