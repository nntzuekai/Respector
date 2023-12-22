package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointMethodConstraint;

import java.util.TreeMap;

import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.Constraints.ConstraintsObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalRefs.GlobalRefObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalWrites.GlobalWriteObj;

import com.google.gson.annotations.SerializedName;

/**
 * GSON serializer @see XEndPointMethodConstraintObjSerializer
 */
public class XEndPointMethodConstraintObj {
  // @SerializedName("valid-path-conditions")
  public ConstraintsObj validPathConstraints;

  // @SerializedName("invalid-path-conditions")
  public ConstraintsObj invalidPathConstraints;

  // @SerializedName("global-reads")
  public TreeMap<Integer, GlobalRefObj> globalReads;

  // @SerializedName("global-writes")
  public TreeMap<Integer, GlobalWriteObj> globalWrites;

  public GlobalRefObj addGlobalRef(GlobalRefObj ref){
    if(this.globalReads==null){
      this.globalReads=new TreeMap<>();
    }

    assert this.globalReads.containsKey(ref.varInfo.id)==false;

    return this.globalReads.put(ref.varInfo.id, ref);
  }

  public GlobalWriteObj addGlobalWrite(GlobalWriteObj wr){
    if(this.globalWrites==null){
      this.globalWrites=new TreeMap<>();
    }

    assert this.globalWrites.containsKey(wr.varInfo.id)==false;

    return this.globalWrites.put(wr.varInfo.id, wr);
  }

  public boolean addValidCond(String cond){
    if(this.validPathConstraints==null){
      this.validPathConstraints=new ConstraintsObj();
    }

    return this.validPathConstraints.addCond(cond);
  }

  public boolean addInvalidCond(String cond){
    if(this.invalidPathConstraints==null){
      this.invalidPathConstraints=new ConstraintsObj();
    }

    return this.invalidPathConstraints.addCond(cond);
  }
}
