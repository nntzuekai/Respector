package org.rest.Respector.SpecGen.Spec.Components.InterDependence;

import java.util.HashMap;

import org.rest.Respector.SpecGen.Spec.ReferenceObj;

import com.google.gson.annotations.SerializedName;

public class GlobalInterDepItemObj {

  @SerializedName("location-details")
  public ReferenceObj gRef;

  @SerializedName("read-by")
  public HashMap<String, ReferenceObj> readers;

  @SerializedName("written-by")
  public HashMap<String, ReferenceObj> writers;

  public GlobalInterDepItemObj(ReferenceObj gRef, HashMap<String, ReferenceObj> readers,
      HashMap<String, ReferenceObj> writers) {
    this.gRef = gRef;
    this.readers = readers;
    this.writers = writers;
  }

  
}
