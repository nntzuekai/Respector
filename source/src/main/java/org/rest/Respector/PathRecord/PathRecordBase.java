package org.rest.Respector.PathRecord;

import soot.SootMethod;

import java.util.ArrayList;

public class PathRecordBase {
  public enum RecordType{
    NotSet,
    Other,
    If,

    // special case of Identity, used only for better logging
    ParamUse,
    ExceptionCatch,

    Assignment,

    // not used in the main loop, only for better logging
    Invoke,

    Throw,
    MethodEntry,
    BackEdge,
    
    // better logging
    Return,

    Goto,
    Identity,

    // DONE: implement SWITCH
    Switch,

    NewInvoke,
    InvokeStmt,
  };

  public RecordType type;
  public ArrayList<SootMethod> callStack;

  public PathRecordBase(){
    this.type=RecordType.NotSet;
    this.callStack=new ArrayList<>();
  }

  public PathRecordBase(RecordType type) {
    this.type=type;
    this.callStack=new ArrayList<>();
  }

  public PathRecordBase(RecordType type, ArrayList<SootMethod> callStack){
    this.type=type;
    this.callStack=callStack;
  }

  public PathRecordBase(PathRecordBase rhs){
    this.type=rhs.type;
    this.callStack=rhs.callStack;
  }
}
