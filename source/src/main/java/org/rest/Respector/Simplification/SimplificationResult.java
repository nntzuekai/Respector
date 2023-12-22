package org.rest.Respector.Simplification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.EndPointParameter;
import org.rest.Respector.PathCondExtract.PathConstraint;

import com.microsoft.z3.Context;

import soot.Local;
import soot.SootField;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Constant;
import soot.jimple.Expr;
import soot.jimple.StaticFieldRef;

public class SimplificationResult {
  public HashMap<EndPointParameter, HashSet<ArrayList<ConditionPred>>> C_epp;
  public HashMap<SootField, HashSet<ArrayList<ConditionPred>>> C_g;
  public HashMap<ArrayList<Value>, HashSet<ConditionPred>> commonPreds;


  public SimplificationResult(HashMap<EndPointParameter, HashSet<ArrayList<ConditionPred>>> C_epp,
      HashMap<SootField, HashSet<ArrayList<ConditionPred>>> C_g,
      HashMap<ArrayList<Value>, HashSet<ConditionPred>> commonPreds) {
    this.C_epp = C_epp;
    this.C_g = C_g;
    this.commonPreds = commonPreds;
  }
 

  public static ArrayList<ArrayList<ConditionPred>> dedupCondPreds(List<PathConstraint> paths) {
    ArrayList<ArrayList<ConditionPred>> deduped=new ArrayList<>();
    for(PathConstraint p: paths){
      ArrayList<ConditionPred> p1= dedupCondPredsInOnePath(p);
      deduped.add(p1);
    }

    return deduped;
  }

  public static ArrayList<ConditionPred> dedupCondPredsInOnePath(PathConstraint path) {
    ArrayList<ConditionPred> deduped=new ArrayList<>();
    HashSet<ConditionPred> existing=new HashSet<>();

    for(ConditionPred pred: path.conds){
      if(pred.inLoopHeader){
        continue;
      }
      if(existing.contains(pred)){
        continue;
      }

      existing.add(pred);
      deduped.add(pred);
    }

    return deduped;
  }

  public static ArrayList<Value> getValueUses(ConditionPred pred) {
    HashSet<Value> values=new HashSet<>();
    
    List<ValueBox> uBoxs = pred.uniCond.getUseBoxes();
    for(ValueBox box: uBoxs){
      Value v = box.getValue();
      if(v instanceof Expr || v instanceof Constant){
        continue;
      }
      else if(v instanceof StaticFieldRef){
        values.add(v);
      }
      else if(v instanceof Local){
        values.add(v);
      }
      /// subsumed by above
      // else if(v instanceof EndPointParameter){
      //   values.add(v);
      // }
    }

    return new ArrayList<>(values);
  }

  @Deprecated
  public static Value hasOnlyEppOrGlobal(ConditionPred pred) {
    Value eppOrG=null;

    List<ValueBox> uBoxs = pred.uniCond.getUseBoxes();

    for(ValueBox box: uBoxs){
      Value v = box.getValue();
      if(v instanceof Expr || v instanceof Constant){
        continue;
      }

      if(v instanceof EndPointParameter){
        if(eppOrG==null){
          eppOrG=v;
        }
        else if(eppOrG==v || eppOrG.equivTo(v)){

        }
        else{
          return null;
        }
      }
      else if(v instanceof StaticFieldRef){
        if(eppOrG==null){
          eppOrG=v;
        }
        else if(eppOrG==v || eppOrG.equivTo(v)){

        }
        else{
          return null;
        }
      }
    }

    return eppOrG;
  }

  public static SimplificationResult doSimplification(ArrayList<PathConstraint> paths) {
    ArrayList<ArrayList<ConditionPred>> deduped=dedupCondPreds(paths);

    HashMap<EndPointParameter, HashSet<ArrayList<ConditionPred>>> C_epp=new HashMap<>();
    HashMap<SootField, HashSet<ArrayList<ConditionPred>>> C_g=new HashMap<>();
    HashMap<ArrayList<Value>, HashMap<ConditionPred, HashSet<Integer>>> IDS=new HashMap<>();
    HashMap<ArrayList<Value>, HashSet<ConditionPred>> commonPreds=new HashMap<>();

    int numPaths=deduped.size();
    for(int i=0;i<numPaths;++i){
      ArrayList<ConditionPred> path= deduped.get(i);

      HashMap<EndPointParameter, ArrayList<ConditionPred>> condsOnEpp=new HashMap<>();
      HashMap<SootField, ArrayList<ConditionPred>> condsOnG=new HashMap<>();

      for(ConditionPred pred: path){
        ArrayList<Value> vUses=getValueUses(pred);

        if(vUses.size()==1){
          Value v0=vUses.get(0);
          if(v0 instanceof EndPointParameter){
            condsOnEpp.computeIfAbsent((EndPointParameter)v0, c->new ArrayList<>()).add(pred);
          }
          else if(v0 instanceof StaticFieldRef){
            SootField f=((StaticFieldRef) v0).getField();
            condsOnG.computeIfAbsent(f, c->new ArrayList<>()).add(pred);
          }
        }

        if(vUses.size()>1){
          HashMap<ConditionPred, HashSet<Integer>> subMap=IDS.computeIfAbsent(vUses, c->new HashMap<>());
          subMap.computeIfAbsent(pred, c->new HashSet<>()).add(i);
        }
      }

      for(Map.Entry<EndPointParameter, ArrayList<ConditionPred>> kv:condsOnEpp.entrySet()){
        EndPointParameter key=kv.getKey();
        
        C_epp.computeIfAbsent(key, c -> new HashSet<>()).add(kv.getValue());
        
      }

      for(Map.Entry<SootField, ArrayList<ConditionPred>> kv:condsOnG.entrySet()){
        SootField key=kv.getKey();
        C_g.computeIfAbsent(key, c->new HashSet<>()).add(kv.getValue());
      }
    }

    for(Map.Entry<ArrayList<Value>, HashMap<ConditionPred, HashSet<Integer>>> kv1: IDS.entrySet()){
      ArrayList<Value> values=kv1.getKey();
      for(Map.Entry<ConditionPred, HashSet<Integer>> kv2: kv1.getValue().entrySet()){
        if(kv2.getValue().size()==numPaths){
          commonPreds.computeIfAbsent(values, c -> new HashSet<>()).add(kv2.getKey());
        }
      }
    }

    return new SimplificationResult(C_epp, C_g, commonPreds);
  }

  // Merge into the this
  public void merge(SimplificationResult res2) {

    HashMap<EndPointParameter, HashSet<ArrayList<ConditionPred>>> C_epp2=res2.C_epp;
    for(Map.Entry<EndPointParameter, HashSet<ArrayList<ConditionPred>>> kv:C_epp2.entrySet()){
      EndPointParameter k=kv.getKey();
      HashSet<ArrayList<ConditionPred>> v=kv.getValue();
      if(C_epp.containsKey(k)){
        C_epp.get(k).addAll(v);
      }
      else{
        C_epp.put(k, v);
      }
    }

    HashMap<SootField, HashSet<ArrayList<ConditionPred>>> C_g2=res2.C_g;
    for(Map.Entry<SootField, HashSet<ArrayList<ConditionPred>>> kv:C_g2.entrySet()){
      SootField k=kv.getKey();
      HashSet<ArrayList<ConditionPred>> v=kv.getValue();
      if(C_g.containsKey(k)){
        C_g.get(k).addAll(v);
      }
      else{
        C_g.put(k, v);
      }
    }

    HashMap<ArrayList<Value>, HashSet<ConditionPred>> commonPreds2=res2.commonPreds;
    HashMap<ArrayList<Value>, HashSet<ConditionPred>> commonPredsNew=new HashMap<>();

    Set<ArrayList<Value>> varSet1=commonPreds.keySet();
    Set<ArrayList<Value>> varSet2=commonPreds2.keySet();

    HashSet<ArrayList<Value>> varSetCommon=new HashSet<>(varSet1);
    varSetCommon.retainAll(varSet2);

    for(ArrayList<Value> vars: varSetCommon){
      HashSet<ConditionPred> predSet1=commonPreds.get(vars);
      HashSet<ConditionPred> predSet2=commonPreds2.get(vars);

      HashSet<ConditionPred> predSetCommon=new HashSet<>(predSet1);
      predSetCommon.retainAll(predSet2);

      if(!predSetCommon.isEmpty()){
        commonPredsNew.put(vars, predSetCommon);
      }
    }

    this.commonPreds=commonPredsNew;
  }
}
