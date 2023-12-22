package org.rest.Respector.PathRecord;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;

import soot.*;
import soot.grimp.internal.ExprBox;
import soot.jimple.*;

public class PathRecord extends PathRecordBase{
  public Stmt stmt;
  public String note;
  public boolean skipInvokeExpr=false;
  public boolean preInvoke=false;

  // In data-flow analysis, usually there are inFlowSet and outFlowSet for each node, indicating the flowSet before and after
  // flowing through the node. For simplicity, we only define inFlowSet here. The same for exceptions.

  // the flowSet *before* flowing thru stmt
  public ImmutableMap<Value, ExprBox> symStore;
  // the exception thrown *before* flowing thru stmt
  public ArrayList<Type> exceptionStack;

  

  public int maxOccur=1;
  public int numOccur=0;

  public PathRecord(Stmt stmt, RecordType type, String note){
    super(type);
    this.stmt=stmt;
    this.note=note;

    this.skipInvokeExpr=false;
    this.preInvoke=false;

    this.symStore=ImmutableMap.of();
    this.exceptionStack=new ArrayList<>();
  }

  public PathRecord(Stmt stmt, RecordType type, String note, 
    ImmutableMap<Value, ExprBox> symStore,
    ArrayList<SootMethod> callStack,
    ArrayList<Type> exceptionStack
    ){
    super(type,callStack);

    this.stmt=stmt;
    this.note=note;

    this.skipInvokeExpr=false;
    this.preInvoke=false;

    this.symStore=symStore;
    this.exceptionStack=exceptionStack;
  }

  public PathRecord(Stmt stmt, RecordType type, String note, 
    ImmutableMap<Value, ExprBox> symStore,
    ArrayList<SootMethod> callStack,
    ArrayList<Type> exceptionStack,
    int maxOccur,
    int numOccur
    ){
    
    super(type, callStack);

    this.stmt=stmt;
    this.note=note;

    this.skipInvokeExpr=false;
    this.preInvoke=false;

    this.symStore=symStore;
    this.exceptionStack=exceptionStack;

    this.maxOccur=maxOccur;
    this.numOccur=numOccur;
  }

  

  public PathRecord(PathRecord rec){
    super(rec);

    this.stmt=rec.stmt;
    this.note=rec.note;

    this.skipInvokeExpr=false;
    this.preInvoke=false;

    this.symStore=rec.symStore;
    this.exceptionStack=rec.exceptionStack;
  }
}
