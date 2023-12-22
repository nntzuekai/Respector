package org.rest.Respector.PrintAllPaths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rest.Respector.EndPointRecog.EndPointMethodInfo;
import org.rest.Respector.EndPointRecog.EndPointParamInfo;
import org.rest.Respector.EndPointRecog.PreprocessFramework;
import org.rest.Respector.LoopInfo.MethodLoopInfo;
import org.rest.Respector.MyPassBase.MyTransformBase;
import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.EndpointAnalysis;
import org.rest.Respector.PathCondExtract.PathConstraint;
import org.rest.Respector.PathRecord.MethodEntryRecord;
import org.rest.Respector.PathRecord.ParamInfo;
import org.rest.Respector.PathRecord.PathRecord;
import org.rest.Respector.PathRecord.PathRecordBase;
import org.rest.Respector.PathRecord.PathRecordBase.RecordType;
import org.rest.Respector.Simplification.PathFeasibility;
import org.rest.Respector.Simplification.SootToZ3;

import soot.Body;
import soot.BriefUnitPrinter;
import soot.Hierarchy;
import soot.Local;
import soot.SceneTransformer;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Scene;
import soot.Value;
import soot.ValueBox;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCaughtExceptionRef;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JThrowStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;

public class PrintAllPaths extends MyTransformBase{
  public PrintAllPaths(PreprocessFramework preprocessReuslt, boolean printBackEdge) {
    super(preprocessReuslt, printBackEdge);
  }

  @Override
  protected void internalTransform(String phaseName, Map<String, String> options) {

    icfg = new JimpleBasedInterproceduralCFG();
    printerSet=new HashMap<Body,BriefUnitPrinter>();
    // hierarchy=Scene.v().getActiveHierarchy();
    bodyToLoopInfoCache=new HashMap<>();

    buildCHACallGraph();

    // int sum=0;

    for(EndPointMethodInfo EPInfo : this.preprocessReuslt.endPointMethodData){
      SootMethod m = EPInfo.method;
      ArrayList<EndPointParamInfo> paramInfo = EPInfo.parameterInfo;

      System.out.printf("%s; %s; ", EPInfo.name, m.getDeclaringClass().getName());

      EndpointAnalysis pass=new EndpointAnalysis(EPInfo, this, 20000);
      pass.buildPaths();

      System.out.printf("%d global variables referred:\n", pass.globalVarRead.size());
      for(SootField f:pass.globalVarRead){
        System.out.printf("    %s.%s\n",f.getDeclaringClass(), f.getName());
      }

      System.out.printf("%d global variables modified:\n", pass.gloablVarWrite.size());
      for(SootField f:pass.gloablVarWrite.keySet()){
        System.out.printf("    %s.%s\n",f.getDeclaringClass(), f.getName());
      }
      System.out.println();


      ArrayList<PathConstraint> validPaths=pass.getValidPathsAndClear();

      System.out.printf("%d paths to valid responses\n\n", validPaths.size());
      // sum+=savedPaths.size();

      // for(PathConstraint path: validPaths){
      //   printPath(path.path);
      //   printConditions(path.conds);
      //   printResponse(path.returnValue);
      //   printResponseValidity(path.isValidPath);
      //   System.out.println();
      // }

      ArrayList<PathConstraint> invalidPaths=pass.getInvalidPathsAndClear();

      System.out.printf("%d paths to invalid responses\n\n", invalidPaths.size());
      // sum+=savedPaths.size();

      // for(PathConstraint path: invalidPaths){
        // PathFeasibility feasibility=new PathFeasibility(path);
        // System.out.println(feasibility.check());

        // printPath(path.path);
        // printConditions(path.conds);
        // printResponse(path.returnValue);
        // printResponseValidity(path.isValidPath);
        // System.out.println();
      // }

      // // if(printBackEdge){
      // //   ArrayList<PathConstraint> backEdgePaths=pass.getBackEdgePaths();

      // //   System.out.printf("%s; %s; %d back edge paths\n\n", EPInfo.name, m.getDeclaringClass().getName(), backEdgePaths.size());
      // //   for(PathConstraint path: backEdgePaths){
      // //     printPath(path.path);
      // //     printConditions(path.conds);
      // //     System.out.println();
      // //   }
      // // }
    }

    // System.out.printf("Average: %f\n", 1.0*sum/endpointsWithParam.size());
  }

}
