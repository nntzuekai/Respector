package org.rest.Respector.PathCondExtract;

import java.util.ArrayList;

import java.util.HashMap;

import org.rest.Respector.PathRecord.PathRecordBase;

import com.google.gson.JsonElement;

import soot.Value;
import soot.jimple.ParameterRef;

public class PathConstraint {
  public ArrayList<PathRecordBase> path;
  public ArrayList<ConditionPred> conds;
  public boolean isValidPath;
  public int HTTPStatusCode;
  public JsonElement responseSchema;

  public Value returnValue;

  public HashMap<EndPointParameter, Value> defaultValMap;

  public PathConstraint(ArrayList<PathRecordBase> path, ArrayList<ConditionPred> conds, boolean isValidPath,
      int hTTPStatusCode, JsonElement responseSchema, Value returnValue, HashMap<EndPointParameter, Value> defaultValMap) {
    this.path = path;
    this.conds = conds;
    this.isValidPath = isValidPath;
    this.HTTPStatusCode = hTTPStatusCode;
    this.responseSchema = responseSchema;
    this.returnValue = returnValue;
    this.defaultValMap = defaultValMap;
  }


  public PathConstraint() {
    this.path=new ArrayList<>();
    this.conds=new ArrayList<>();
    this.isValidPath=true;
    this.HTTPStatusCode=200;
    this.responseSchema=null;

    this.returnValue=null;
    this.defaultValMap=new HashMap<>();
  }

  public PathConstraint(PathConstraint src){
    this.path=new ArrayList<>(src.path);
    this.conds=new ArrayList<>(src.conds);
    this.isValidPath=src.isValidPath;
    this.HTTPStatusCode=src.HTTPStatusCode;
    this.responseSchema=src.responseSchema;

    this.returnValue=src.returnValue;
    this.defaultValMap=src.defaultValMap;
  }

  boolean pathEmpty(){
    return this.path.isEmpty();
  }

  PathRecordBase getPathBack(){
    return this.path.get(this.path.size()-1);
  }

  boolean addToPath(PathRecordBase node){
    return this.path.add(node);
  }

  PathRecordBase get(int i){
    return this.path.get(i);
  }

  boolean addToCond(ConditionPred pred){
    return this.conds.add(pred);
  }
}
