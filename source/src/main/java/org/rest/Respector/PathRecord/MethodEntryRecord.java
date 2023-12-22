package org.rest.Respector.PathRecord;

import soot.SootMethod;

import java.util.ArrayList;

public class MethodEntryRecord extends PathRecordBase{
  public SootMethod method;
  public ArrayList<ParamInfo> paramInfo;

  public MethodEntryRecord(SootMethod method, ArrayList<ParamInfo> paramInfo, ArrayList<SootMethod> callStack){
    super(RecordType.MethodEntry, callStack);
    
    this.method=method;
    this.paramInfo=paramInfo;
  }
}
