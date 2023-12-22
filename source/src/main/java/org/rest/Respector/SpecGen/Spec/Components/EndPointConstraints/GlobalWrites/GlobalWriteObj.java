package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalWrites;

import java.util.ArrayList;
import java.util.HashSet;

import org.rest.Respector.SpecGen.Spec.ReferenceObj;
import org.rest.Respector.SpecGen.Spec.Components.GlobalVarInfo;

import com.google.gson.annotations.SerializedName;

/**
 * GSON serializer @see GlobalWriteObjSerializer
 */
public class GlobalWriteObj {
  public final transient GlobalVarInfo varInfo;

  public HashSet<String> values;

  public final transient ReferenceObj ref;

  public GlobalWriteObj(GlobalVarInfo varInfo) {
    this.varInfo = varInfo;
    this.ref=varInfo.refToMe;
  }

  public boolean addValue(String value) {
    if(this.values==null){
      this.values=new HashSet<>();
    }

    return this.values.add(value);
  }
}
