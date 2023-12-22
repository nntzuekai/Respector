package org.rest.Respector.PathCondExtract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.management.RuntimeErrorException;

import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.SootMethodRefImpl;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.grimp.Grimp;
import soot.grimp.internal.ExprBox;
import soot.grimp.internal.GNewInvokeExpr;
import soot.Body;
import soot.Trap;
import soot.Type;
import soot.util.Chain;
import soot.Hierarchy;
import soot.Immediate;
import soot.MethodOrMethodContext;
import soot.jimple.IdentityRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.ConditionExpr;
import soot.jimple.Constant;
import soot.jimple.Expr;
import soot.jimple.SwitchStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCaughtExceptionRef;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JLookupSwitchStmt;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JTableSwitchStmt;
import soot.jimple.internal.JThrowStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Targets;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;

import org.apache.commons.lang3.tuple.Pair;
import org.rest.Respector.EndPointRecog.EndPointMethodInfo;
import org.rest.Respector.EndPointRecog.EndPointParamInfo;
import org.rest.Respector.EndPointRecog.FrameworkData;
import org.rest.Respector.EndPointRecog.ResponseBuilderInfo;
import org.rest.Respector.EndPointRecog.StaticResponseInfo;
import org.rest.Respector.LoopInfo.MethodLoopInfo;
import org.rest.Respector.MyPassBase.MyTransformBase;
import org.rest.Respector.PathCondExtract.ConditionPred.ConstantComp;
import org.rest.Respector.PathRecord.MethodEntryRecord;
import org.rest.Respector.PathRecord.ParamInfo;
import org.rest.Respector.PathRecord.PathRecord;
import org.rest.Respector.PathRecord.PathRecordBase;
import org.rest.Respector.PathRecord.PathRecordBase.RecordType;
import org.rest.Respector.Simplification.PathFeasibility;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Responses.ResponseSchemaGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

public class EndpointAnalysis {
  public final EndPointMethodInfo EPInfo;
  public final SootMethod method;
  // public final ArrayList<Integer> paramIndices;
  public final TreeMap<Integer, String> paramNames;
  public final JimpleBasedInterproceduralCFG icfg;
  // public final Hierarchy hierarchy;
  public final CallGraph cg;
  public final HashMap<Body, MethodLoopInfo> bodyToLoopInfoCache;
  public final FrameworkData frameworkData;

  // whenever you want to add a path to savedPaths, backEdgePaths or cachedPaths, make a copy
  ArrayList<PathConstraint> validPaths = new ArrayList<>();
  ArrayList<PathConstraint> invalidPaths = new ArrayList<>();
  // public ArrayList<PathConstraint> backEdgePaths = new ArrayList<>();
  ArrayList<PathConstraint> cachedPaths = new ArrayList<>();

  public int chunkFeasiblePaths=0;
  public int chunkAllPaths=0;
  public int totalPaths=0;
  public final int pathThresh;
  boolean firstInvoke=true;

  // you can add/remove items to currPath without making a copy
  PathConstraint currPath = new PathConstraint();

  public HashSet<SootField> globalVarRead = new HashSet<>();
  public HashMap<SootField, ArrayList<Value>> gloablVarWrite=new HashMap<>();

  private static Logger logger = LoggerFactory.getLogger(EndpointAnalysis.class);

  static <K, V> ImmutableMap<K, V> putIntoImmutableMap(ImmutableMap<K,V> map, K key, V value){
    if(map.containsKey(key)){
      ImmutableMap.Builder<K,V> builder=ImmutableMap.builderWithExpectedSize(map.size());
      for(Map.Entry<K,V> kv: map.entrySet()){
        if(kv.getKey().equals(key)){
          builder.put(key, value);
        }
        else{
          builder.put(kv);
        }
      }

      return builder.build();
    }
    else{
      ImmutableMap.Builder<K,V> builder=ImmutableMap.<K,V>builder().putAll(map);
      builder.put(key, value);
      return builder.build();
    }
  }

  public EndpointAnalysis(EndPointMethodInfo EPInfo, 
    // ArrayList<Integer> paramIndices, 
    TreeMap<Integer, String> paramNames, JimpleBasedInterproceduralCFG icfg, CallGraph cg, HashMap<Body, MethodLoopInfo> bodyToLoopInfoCache, FrameworkData frameworkData, int pathThresh) {
    
    this.EPInfo=EPInfo;
    this.method = EPInfo.method;
    // this.paramIndices = paramIndices;
    this.paramNames = paramNames;
    this.icfg = icfg;
    // this.hierarchy=hierarchy;
    this.cg=cg;
    this.bodyToLoopInfoCache=bodyToLoopInfoCache;
    this.frameworkData=frameworkData;
    this.pathThresh=pathThresh;
  }

  public EndpointAnalysis(EndPointMethodInfo EPInfo, MyTransformBase myApp, int pathThresh) {
    this.EPInfo=EPInfo;
    this.method = EPInfo.method;
    // this.paramIndices = new ArrayList<>();
    this.paramNames = new TreeMap<>();
    this.icfg = myApp.icfg;
    // this.hierarchy=myApp.hierarchy;
    this.cg=myApp.CHA_CG;
    this.bodyToLoopInfoCache=myApp.bodyToLoopInfoCache;
    this.frameworkData=myApp.preprocessReuslt.frameworkData;
    this.pathThresh=pathThresh;

    for(EndPointParamInfo i: EPInfo.parameterInfo){
      // this.paramIndices.add(i.index);
      this.paramNames.put(i.index, i.name);
    }
  }

  public ArrayList<PathConstraint> getValidPathsAndClear() {
    ArrayList<PathConstraint> rtv=this.validPaths;
    this.validPaths=new ArrayList<>();
    return rtv;
  }

  public ArrayList<PathConstraint> getInvalidPathsAndClear() {
    ArrayList<PathConstraint> rtv=this.invalidPaths;
    this.invalidPaths=new ArrayList<>();
    return rtv;
  }

  ArrayList<SootMethod> getCalleesAt(Unit u){
    // getCalleesOfCallAt only returns callees with bodies, 
    // if setIncludePhantomCallees is not called
    ArrayList<SootMethod> l1= new ArrayList<>(icfg.getCalleesOfCallAt(u));

    if(!l1.isEmpty()){
      return l1;
    }

    Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(u));

    while (targets.hasNext()) {
      SootMethod tgt = (SootMethod) targets.next();
      if(tgt.hasActiveBody()){
        l1.add(tgt);
      }
    }

    return l1;
  }

  // public ArrayList<PathConstraint> getSavedPaths() {
  //   return this.savedPaths;
  // }

  // public ArrayList<PathConstraint> getBackEdgePaths() {
  //   return this.backEdgePaths;
  // }

  // public Stream<PathConstraint> getAllPaths(){
  //   return Stream.concat(savedPaths.stream(), backEdgePaths.stream());
  // }

  public boolean buildPaths() {
    if(firstInvoke){
      firstInvoke=false;
      initMethodEntry();
    }

    while (true) {
      if(totalPaths>pathThresh){
        logger.debug(String.format("path number over threshold %d",pathThresh));
        return false;
      }

      if(chunkFeasiblePaths>1000){
        chunkFeasiblePaths=0;
        return true;
      }
      // if(this.validPaths.size()>1000){
      //   return;
      // }

      // get a cached unfinished path
      if (currPath == null || currPath.pathEmpty()) {
        if (cachedPaths.isEmpty()) {
          // all done!
          break;
        } else {
          PathConstraint p = cachedPaths.get(cachedPaths.size() - 1);
          cachedPaths.remove(cachedPaths.size() - 1);

          currPath = p;
        }
      }

      PathRecordBase lastNode = currPath.getPathBack();

      if((lastNode instanceof PathRecord)){
        PathRecord n2=(PathRecord) lastNode;


        // if(decideInitInvoke(n2.stmt)){
        //   handleSpecialInvokeExpr();
        //   continue;
        // }

        if(n2.skipInvokeExpr!=true){
          boolean handled = handleInvokeExpr();

          if(handled){
            continue;
          }
        }
      }
      /// TODO: need to be PathRecord?
      if((lastNode instanceof PathRecord)){
        PathRecord n2=(PathRecord) lastNode;
        if(n2.exceptionStack!=null && !n2.exceptionStack.isEmpty() && n2.type!=RecordType.Identity){
          handleException();
          continue;
        }
      }

      
      
      switch (lastNode.type) {
        case MethodEntry:
          handleMethodEntry();
          break;
        
        case If:
          handleIf();
          break;
        
        case Goto:
          handleGoto();
          break;
        
        case Identity:
          handleIdentity();
          break;
        
        case Assignment:
          handleAssignment();
          break;
        
        case Throw:
          handleThrow();
          break;
        
        case Switch:
          handleSwitch();
          break;

        case NewInvoke:
          handleNewInvoke();
          break;

        case InvokeStmt:
          handleInvokeStmt();
          break;

        case Other:
          handleOther();
          break;

        case BackEdge:
          throw new RuntimeException("BackEdge in cachedPaths?!");

        default:
          throw new RuntimeException("RecordType not implemented?");
          // break;
      }
    }

    return false;
  }

  void savePath(PathConstraint path){
    this.totalPaths+=1;
    this.chunkAllPaths+=1;
    if(this.totalPaths%1000==0){
      logger.info("total paths analyzed: "+this.totalPaths);
    }
    PathFeasibility feasibility=new PathFeasibility(path);
    if(feasibility.check()){
      this.chunkFeasiblePaths+=1;
      if(path.isValidPath){
        this.validPaths.add(path);
      }
      else{
        this.invalidPaths.add(path);
      }
    }
  }

  // always use this to save a path
  // DO NOT append to currPath directly
  // becaue checking backedges only happens here
  void cacheOrDisgardPath(PathConstraint path){
    PathRecordBase lastNode=path.getPathBack();

    if (lastNode.type == RecordType.BackEdge) {
      // backEdgePaths.add(path);
    } else {
      cachedPaths.add(path);
    }
  }
  // DOES NOT check back edges
  void cachePath(PathConstraint path){
    cachedPaths.add(path);
  }

  public void handleSwitch() {
    PathRecord lastNode = (PathRecord)currPath.getPathBack();
    SwitchStmt stmt0=(SwitchStmt) lastNode.stmt;

    Body body=icfg.getBodyOf(stmt0);
    HashSet<Stmt> headerBlockStmts = this.bodyToLoopInfoCache.get(body).headerBlockStmts;

    if(stmt0 instanceof JLookupSwitchStmt){
      JLookupSwitchStmt stmt=(JLookupSwitchStmt) stmt0;

      Value key=stmt.getKey();
      List<Unit> targets=stmt.getTargets();
      List<IntConstant> lookups=stmt.getLookupValues();
      Unit defaultTarget=stmt.getDefaultTarget();

      if(key instanceof StaticFieldRef){
        StaticFieldRef key1= (StaticFieldRef)key;
        this.globalVarRead.add(key1.getField());
        
      }
      /// DONE:
      // String keyName=ValueRewrite.rewriteValueWith(key, lastNode.symStore, body);
      if(key instanceof Expr){
        key=RhsRewrite.rewriteExpr((Expr)key, lastNode.symStore).getValue();
      }
      String keyName=key.toString();

      int len=lookups.size();
      assert(len==targets.size());

      for(int i=0;i<len;++i){
        Stmt succ=(Stmt) targets.get(i);

        PathConstraint pathCase=new PathConstraint(currPath);
        PathRecord caseNode=new PathRecord(lastNode);
        
        int keyVal=lookups.get(i).value;
        caseNode.note=String.format("-- case %s == %d", keyName, keyVal);
        pathCase.path.set(pathCase.path.size()-1, caseNode);

        ConditionPred cond= new ConditionPred(key, keyVal, keyName);
        ConstantComp checkComp= ConditionPred.checkConstantComparison(cond);

        if(checkComp==ConstantComp.EvalFalse){
          continue;
        }

        if(checkComp==ConstantComp.NonConstant){
          pathCase.addToCond(cond);

          PathFeasibility feasibility=new PathFeasibility(pathCase);
          if(!feasibility.check()){
            continue;
          }
        }

        PathRecord outNode=new PathRecord(lastNode);
        outNode.stmt=succ;
        int numOccur=numOccurence(succ, currPath.path)+1;
        int maxOccur=headerBlockStmts.contains(succ)?MethodLoopInfo.maxOccurForHeader:1;
        outNode.numOccur=numOccur;
        outNode.maxOccur=maxOccur;
        outNode.type=decideRecordType(succ, numOccur>maxOccur, pathCase.path);
        outNode.note="";
        pathCase.addToPath(outNode);

        cacheOrDisgardPath(pathCase);
      }

      /// TODO: how to add cond for default branch?
      if(defaultTarget!=null){
        Stmt succ=(Stmt) defaultTarget;

        PathConstraint pathCase=new PathConstraint(currPath);
        PathRecord caseNode=new PathRecord(lastNode);
        caseNode.note=String.format("-- default case for %s", keyName);
        pathCase.path.set(pathCase.path.size()-1, caseNode);

        PathRecord outNode=new PathRecord(lastNode);
        outNode.stmt=succ;
        int numOccur=numOccurence(succ, currPath.path)+1;
        int maxOccur=headerBlockStmts.contains(succ)?MethodLoopInfo.maxOccurForHeader:1;
        outNode.numOccur=numOccur;
        outNode.maxOccur=maxOccur;
        outNode.type=decideRecordType(succ, numOccur>maxOccur, pathCase.path);
        outNode.note="";
        pathCase.addToPath(outNode);

        cacheOrDisgardPath(pathCase);
      }

      currPath=null;
    }
    else{
      JTableSwitchStmt stmt=(JTableSwitchStmt) stmt0;

      Value key=stmt.getKey();
      List<Unit> targets=stmt.getTargets();
      int i0=stmt.getLowIndex();
      int i1=stmt.getHighIndex();

      // String keyName=ValueRewrite.rewriteValueWith(key, lastNode.symStore, body);
      if(key instanceof Expr){
        key=RhsRewrite.rewriteExpr((Expr)key, lastNode.symStore).getValue();
      }
      String keyName=key.toString();

      Unit defaultTarget=stmt.getDefaultTarget();

      assert(i1-i0+1==targets.size());

      // Don't use i<=i1 due to potential wrap around
      for(int i=i0;i<i1;++i){
        Stmt succ=(Stmt) targets.get(i-i0);

        PathConstraint pathCase=new PathConstraint(currPath);
        PathRecord caseNode=new PathRecord(lastNode);
        caseNode.note=String.format("-- case %s == %d", keyName, i);
        pathCase.path.set(pathCase.path.size()-1, caseNode);

        ConditionPred cond= new ConditionPred(key, i, keyName);
        ConstantComp checkComp= ConditionPred.checkConstantComparison(cond);

        if(checkComp==ConstantComp.EvalFalse){
          continue;
        }

        if(checkComp==ConstantComp.NonConstant){
          pathCase.addToCond(cond);

          PathFeasibility feasibility=new PathFeasibility(pathCase);
          if(!feasibility.check()){
            continue;
          }
        }

        PathRecord outNode=new PathRecord(lastNode);
        outNode.stmt=succ;
        int numOccur=numOccurence(succ, currPath.path)+1;
        int maxOccur=headerBlockStmts.contains(succ)?MethodLoopInfo.maxOccurForHeader:1;
        outNode.numOccur=numOccur;
        outNode.maxOccur=maxOccur;
        outNode.type=decideRecordType(succ, numOccur>maxOccur, pathCase.path);
        outNode.note="";
        pathCase.addToPath(outNode);

        cacheOrDisgardPath(pathCase);
      }

      // i==i1
      {
        Stmt succ=(Stmt) targets.get(i1-i0);

        PathConstraint pathCase=new PathConstraint(currPath);
        PathRecord caseNode=new PathRecord(lastNode);
        caseNode.note=String.format("-- case %s == %d", keyName, i1);
        pathCase.path.set(pathCase.path.size()-1, caseNode);

        ConditionPred cond= new ConditionPred(key, i1, keyName);
        ConstantComp checkComp= ConditionPred.checkConstantComparison(cond);


        boolean pathOK=true;
        if(checkComp==ConstantComp.EvalFalse){
          pathOK=false;
        }

        if(checkComp==ConstantComp.NonConstant){
          pathCase.addToCond(cond);

          PathFeasibility feasibility=new PathFeasibility(pathCase);
          pathOK= feasibility.check();
        }

        if(pathOK){
          PathRecord outNode=new PathRecord(lastNode);
          outNode.stmt=succ;
          int numOccur=numOccurence(succ, currPath.path)+1;
          int maxOccur=headerBlockStmts.contains(succ)?MethodLoopInfo.maxOccurForHeader:1;
          outNode.numOccur=numOccur;
          outNode.maxOccur=maxOccur;
          outNode.type=decideRecordType(succ, numOccur>maxOccur, pathCase.path);
          outNode.note="";
          pathCase.addToPath(outNode);
  
          cacheOrDisgardPath(pathCase);
        }
      }

      /// TODO: how to add cond for default branch?
      // key<low && key>high
      if(defaultTarget!=null){
        Stmt succ=(Stmt) defaultTarget;

        PathConstraint pathCase=new PathConstraint(currPath);
        PathRecord caseNode=new PathRecord(lastNode);
        caseNode.note=String.format("-- default case for %s", keyName);
        pathCase.path.set(pathCase.path.size()-1, caseNode);

        PathRecord outNode=new PathRecord(lastNode);
        outNode.stmt=succ;
        int numOccur=numOccurence(succ, currPath.path)+1;
        int maxOccur=headerBlockStmts.contains(succ)?MethodLoopInfo.maxOccurForHeader:1;
        outNode.numOccur=numOccur;
        outNode.maxOccur=maxOccur;
        outNode.type=decideRecordType(succ, numOccur>maxOccur, pathCase.path);
        outNode.note="";
        pathCase.addToPath(outNode);

        cacheOrDisgardPath(pathCase);
      }

      currPath=null;
    }
  }

  public void handleThrow() {
    PathRecord lastNode = (PathRecord) currPath.path.get(currPath.path.size() - 1);
    JThrowStmt stmt=(JThrowStmt) lastNode.stmt;
    lastNode.note="-- THROW";

    Value exceptVal=stmt.getOp();
    ArrayList<Type> newExceptionStack=new ArrayList<>(lastNode.exceptionStack);
    newExceptionStack.add(exceptVal.getType());

    expandSuccsExceptional(lastNode, newExceptionStack);
  }

  public static boolean checkAppClass(SootMethod method) {
    if(method.getDeclaringClass().getPackageName().equals("java.lang")){
      return false;
    }
    // exclude inner classes whose names contain '$'
    else if(method.getDeclaringClass().getShortName().contains("$")){
      return false;
    }
    else{
      return true;
    }
  }

  // public void handleSpecialInvokeExpr() {
  //   PathRecord lastNode = (PathRecord) currPath.path.get(currPath.path.size() - 1);
  //   JInvokeStmt stmt = (JInvokeStmt) lastNode.stmt;
  //   HashMap<Value, ExprBox> symStore = lastNode.symStore;

  //   InvokeExpr expr = stmt.getInvokeExpr();

  //   // assert (expr instanceof JSpecialInvokeExpr);

  //   JSpecialInvokeExpr speExpr=(JSpecialInvokeExpr) expr;
  //   Value base=speExpr.getBase();

  //   // assert (symStore.containsKey(base));

  //   HashMap<Value, ExprBox> newSymStore=new HashMap<>(symStore);

  //   newSymStore.put(base, speExpr);

  //   expandSuccs(lastNode, newSymStore);
  // }

  public boolean handleInvokeExpr() {
    PathRecord lastNode = (PathRecord) currPath.path.get(currPath.path.size() - 1);
    Stmt stmt = lastNode.stmt;

    if(stmt.containsInvokeExpr()==false){
      return false;
    }
    

    InvokeExpr expr = stmt.getInvokeExpr();
    assert (expr != null);

    // if(expr.getMethod().getName().equals("checkArgument")){
    //   logger.info(expr.getMethod().toString());
    // }

    if(expr instanceof InstanceInvokeExpr){
      InstanceInvokeExpr instExpr=(InstanceInvokeExpr) expr;
      Value base = instExpr.getBase();

      if(ConditionPred.evalToNullConstant(base)==ConstantComp.EvalTrue){
        try{
          Type nullPtrExcpType = Scene.v().getType("java.lang.NullPointerException");

          ArrayList<Type> newExceptionStack=new ArrayList<>(lastNode.exceptionStack);
          newExceptionStack.add(nullPtrExcpType);

          //handled
          return true;
        }
        catch(RuntimeException e){
          currPath.isValidPath=false;
          currPath.HTTPStatusCode=500;
          savePath(currPath);
          currPath=null;

          //handled
          return true;
        }
      }
    }

    ArrayList<SootMethod> callees = this.getCalleesAt(stmt);
    // appCallees.size()>0 already checked
    Stream<SootMethod> appCallees = callees.stream()
      .filter(m -> checkAppClass(m))
    ;

    Iterator<SootMethod> iter = appCallees.iterator();
    if (!iter.hasNext()) {
      lastNode.skipInvokeExpr = true;
      return false;
    }


    ImmutableMap<Value, ExprBox> symStore = lastNode.symStore;
    ArrayList<SootMethod> callStack = lastNode.callStack;
    ArrayList<Type> exceptionStack = lastNode.exceptionStack;


    List<Value> exprArgs = expr.getArgs();

    ArrayList<ParamInfo> callParamIdx = new ArrayList<>();

    for(int i=0;i<exprArgs.size();++i){
      Value arg_i=exprArgs.get(i);
      if(symStore.containsKey(arg_i)){
        ParamInfo info=new ParamInfo(i, symStore.get(arg_i).getValue());
        callParamIdx.add(info);
      }
      else{
        ParamInfo info=new ParamInfo(i, arg_i);
        callParamIdx.add(info);
      }
    }

    /// TODO: callParamIdx can not be empty now, unless the call has no argument itself.
    if (callParamIdx.isEmpty()) {
      lastNode.skipInvokeExpr = true;
      return false;
    }


    lastNode.preInvoke=true;
    
    PathRecord invokeNode = new PathRecord(stmt, RecordType.Invoke,
        String.format(">> Invoke to %d impl, param %s in symStore", callees.size(), 
          callParamIdx.stream().map(c -> c.idx).collect(Collectors.toCollection(ArrayList::new)).toString()),
        symStore, callStack, exceptionStack);

    while (iter.hasNext()) {
      SootMethod callee = iter.next();

      // if(callee.getName().equals("<clinit>")){
      //   System.err.println("here");
      // }

      PathConstraint newPath = new PathConstraint(currPath);
      newPath.addToPath(invokeNode);

      MethodEntryRecord entryNode = new MethodEntryRecord(callee, callParamIdx, callStack);
      newPath.addToPath(entryNode);

      cacheOrDisgardPath(newPath);
    }

    currPath = null;
    
    return true;
  }

  public void handleException() {
    PathRecord lastNode = (PathRecord) currPath.path.get(currPath.path.size() - 1);
    expandSuccsExceptional(lastNode);
  }

  public void handleOther() {
    PathRecord lastNode = (PathRecord) currPath.path.get(currPath.path.size() - 1);

    expandSuccs(lastNode);
  }

  public void handleInvokeStmt() {
    PathRecord lastNode = (PathRecord) currPath.path.get(currPath.path.size() - 1);

    JInvokeStmt stmt=(JInvokeStmt) lastNode.stmt;

    expandSuccs(lastNode);
  }

  public void handleNewInvoke() {
    PathRecord lastNode = (PathRecord) currPath.path.get(currPath.path.size() - 1);
    JInvokeStmt stmt=(JInvokeStmt) lastNode.stmt;
    ImmutableMap<Value, ExprBox> symStore = lastNode.symStore;

    JSpecialInvokeExpr expr = (JSpecialInvokeExpr) stmt.getInvokeExpr();
    assert (expr.getMethodRef().getName().equals("<init>"));
    Value base = expr.getBase();

    if(base instanceof Local){
      Local base1=(Local) base;
      if(base1.getName().equals("this")){
        expandSuccs(lastNode);
        return;
      }
    }

    List<Value> args=expr.getArgs().stream().map(v -> RhsRewrite.rewriteRHS(v, symStore).getValue()).collect(Collectors.toList());

    assert symStore.containsKey(base);

    ExprBox baseBox=symStore.get(base);
    Value baseV=baseBox.getValue();

    assert baseV instanceof NewExpr;

    NewExpr oldRhs=(NewExpr)baseV;

    GNewInvokeExpr newRhs=new GNewInvokeExpr(oldRhs.getBaseType(), expr.getMethodRef(), args);

    /// DONE: 
    //  verify if we can modify the original symStore directly.
    // symstore is now immutatble
    
    ExprBox exprBox=RhsRewrite.rewriteRHS(base, symStore);
    ExprBox newBox =new ExprBox(newRhs);

    /// TODO: Workaround
    ImmutableMap.Builder<Value, ExprBox> builder=ImmutableMap.builderWithExpectedSize(symStore.size());
    for(Map.Entry<Value, ExprBox> kv: symStore.entrySet()){
      if(kv.getValue()==exprBox){
        builder.put(kv.getKey(), newBox);
      }
      else{
        builder.put(kv);
      }
    }

    expandSuccs(lastNode, builder.build());
  }

  public void handleAssignment() {
    PathRecord lastNode = (PathRecord) currPath.path.get(currPath.path.size() - 1);
    JAssignStmt stmt=(JAssignStmt) lastNode.stmt;
    ImmutableMap<Value, ExprBox> symStore = lastNode.symStore;

    Value lhs=stmt.getLeftOp();
    Value rhs=stmt.getRightOp();
    // ArrayList<ValueBox> useVBs=new ArrayList<>(rhs.getUseBoxes());
    // useVBs.add(stmt.getRightOpBox());

    // consider Constant values as well
    // boolean inFlowSet = useVBs.stream().anyMatch(vb -> {
    //   Value v=vb.getValue();
    //   return (v instanceof Local && symStore.containsKey((Local)v))
    //   || (v instanceof Constant);
    // });

    
    ExprBox rhsRewrite=RhsRewrite.rewriteRHS(rhs, symStore);

    // if(rhsRewrite instanceof JNewExpr){
      // JNewExpr newRHS=(JNewExpr) rhsRewrite;
      // SootMethod initRHS = newRHS.getBaseType().getSootClass().getMethodByName("<init>");
      // SootMethodRefImpl initRef=new SootMethodRefImpl(initRHS.getDeclaringClass(), initRHS.getName(), initRHS.getParameterTypes(), initRHS.getReturnType(), initRHS.isStatic());
      // rhsRewrite=new JSpecialInvokeExpr((Local)lhs, initRef , new ArrayList<Immediate>());

      // System.out.println("here JNewExpr");
    // }
    
    ImmutableMap<Value, ExprBox> newSymStore=putIntoImmutableMap(symStore, lhs, rhsRewrite);

    if(lhs instanceof StaticFieldRef){
      ArrayList<Value> mappedVals = this.gloablVarWrite.computeIfAbsent(((StaticFieldRef)lhs).getField(), r -> new ArrayList<>());
      mappedVals.add(rhsRewrite.getValue());
    }

    if(rhs instanceof StaticFieldRef){
      this.globalVarRead.add(((StaticFieldRef) rhs).getField());
    }

    if(lhs instanceof EndPointParameter && !currPath.defaultValMap.containsKey(lhs)){
      currPath.defaultValMap.put((EndPointParameter)lhs, rhsRewrite.getValue());
    }
    
    // if(inFlowSet){
    //   Value lhs=stmt.getLeftOp();

    //   newFlowSet=new HashMap<>(symStore);
      
    //   if(lhs instanceof Local){
    //     ValueRewrite rewt= new ValueRewrite();
    //     rewt.v=stmt.getRightOp();
    //     rewt.str=ValueRewrite.rewriteValueWith(stmt.getRightOp(), symStore, icfg.getBodyOf(stmt));
    //     newFlowSet.put((Local) lhs, rewt);

    //     lastNode.note=String.format("-- Assignment %s", rewt.str);
    //   }
    //   else if(lhs instanceof JInstanceFieldRef){
    //     JInstanceFieldRef lhs1=(JInstanceFieldRef) lhs;

    //     ValueRewrite rewt= new ValueRewrite();
    //     rewt.v=stmt.getRightOp();
    //     rewt.str=ValueRewrite.rewriteValueWith(stmt.getRightOp(), symStore, icfg.getBodyOf(stmt));

    //     newFlowSet.put((Local) lhs1.getBase(), rewt);

    //     lastNode.note=String.format("-- Assignment %s", rewt.str);
    //   }

    //   lastNode.note="-- Assignment from symStore";
    // }
    // else{
    //   newFlowSet=symStore;
    // }

    expandSuccs(lastNode, newSymStore);
  }

  // See below
  public void expandSuccs(PathRecord lastNode){
    expandSuccs(lastNode, lastNode.symStore);
  }

  public void expandSuccsExceptional(PathRecord lastNode){
    expandSuccsExceptional(lastNode, lastNode.exceptionStack);
  }

  public void expandSuccsExceptional(PathRecord lastNode, ArrayList<Type> newExceptionStack) {
    Stmt stmt=lastNode.stmt;
    Body body=icfg.getBodyOf(stmt);
    DirectedGraph<Unit> d_graph = icfg.getOrCreateUnitGraph(body);
    assert (d_graph instanceof ExceptionalUnitGraph);
    ExceptionalUnitGraph graph = (ExceptionalUnitGraph) d_graph;

    // omitExceptingUnitEdges is true by default
    /// TODO: if we only want to go to the catch block that actually catches the exception,
    /// i.e., the first catch block whose exception type is the super class of the exception,
    /// we would need to filter it out here before adding all catch blocks into cachedPaths.
    List<Unit> succList=graph.getExceptionalSuccsOf(stmt);
    
    //unwinding
    /// TODO: add retval to the symStore of return site

    if(succList.isEmpty()){
      if (lastNode.callStack.size() == 1) {
        currPath.isValidPath=false;
        /// TODO:
        currPath.HTTPStatusCode=500;
        savePath(currPath);
        currPath = null;
      }
      else{
        int stackHeight=lastNode.callStack.size();

        int len = currPath.path.size();
        int i;
        for (i = len - 1; i >= 0; --i) {
          PathRecordBase ri=currPath.get(i);
          if (ri.type == RecordType.Invoke && ri.callStack.size()==stackHeight-1) {
            break;
          }
        }

        assert (i > 0);
        /// TODO: must be PathRecord?
        PathRecordBase n0=currPath.get(i - 1);
        assert(n0 instanceof PathRecord);
        PathRecord preInvokeNode = new PathRecord((PathRecord)n0);
        
        preInvokeNode.skipInvokeExpr = true;
        preInvokeNode.exceptionStack=newExceptionStack;
        preInvokeNode.note="-- Unwinding";

        currPath.addToPath(preInvokeNode);
        cachePath(currPath);
        currPath = null;
      }
    }
    else{
      for(Unit succU: succList){
        assert(succU instanceof JIdentityStmt);

        JIdentityStmt succ=(JIdentityStmt) succU;

        PathRecord outNode = new PathRecord(lastNode);
        outNode.stmt = succ;
        outNode.type=RecordType.Identity;
        outNode.note="";
        outNode.exceptionStack=newExceptionStack;
        
        PathConstraint pathOut=new PathConstraint(currPath);
        pathOut.addToPath(outNode);

        cacheOrDisgardPath(pathOut);
      }

      currPath=null;
    }
  }

  // expand on all succs
  // set currPath to null
  /// DONE: DOES NOT consider exceptions
  /// DONE: DOES consider function returns
  public void expandSuccs(PathRecord lastNode, ImmutableMap<Value, ExprBox> newFlowSet) {
    if(!lastNode.exceptionStack.isEmpty()){
      expandSuccsExceptional(lastNode);
      return;
    }

    Stmt stmt=lastNode.stmt;
    Body body=icfg.getBodyOf(stmt);
    DirectedGraph<Unit> d_graph = icfg.getOrCreateUnitGraph(body);
    assert (d_graph instanceof ExceptionalUnitGraph);
    ExceptionalUnitGraph graph = (ExceptionalUnitGraph) d_graph;
    ImmutableMap<Value, ExprBox> symStore0=lastNode.symStore;

    // exclude exceptional edges
    List<Unit> succList=graph.getUnexceptionalSuccsOf(stmt);

    if (succList.isEmpty()) {
      assert (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt);

      /// DONE: function returns
      /// DONE: add retval to the symStore of return site
      lastNode.type = RecordType.Return;

      if (lastNode.callStack.size() == 1) {
        /// DONE: determine if response is valid

        if(stmt instanceof JReturnStmt){
          JReturnStmt rtStmt=(JReturnStmt) stmt;
          Value rtv=rtStmt.getOp();

          if(symStore0.containsKey(rtv)){
            Value rr=RhsRewrite.rewriteRHS(rtv, symStore0).getValue();

            currPath.returnValue=rr;
          }
          else{
            currPath.returnValue=rtv;
          }
        }

        Pair<Integer, JsonElement> codeAndSchema=genResponseCodeAndSchema(currPath);
        currPath.HTTPStatusCode=codeAndSchema.getLeft();
        currPath.responseSchema=codeAndSchema.getRight();

        currPath.isValidPath= ( currPath.HTTPStatusCode<400 );

        savePath(currPath);
        currPath = null;
      } else {
        int stackHeight=lastNode.callStack.size();

        int len = currPath.path.size();
        int i;
        for (i = len - 1; i >= 0; --i) {
          PathRecordBase ri=currPath.get(i);
          if (ri.type == RecordType.Invoke && ri.callStack.size()==stackHeight-1) {
            break;
          }
        }

        assert (i > 0);
        /// TODO: must be PathRecord?
        PathRecordBase n0=currPath.get(i - 1);
        assert (n0 instanceof PathRecord);
        PathRecord preInvokeNode = new PathRecord((PathRecord)n0);

        preInvokeNode.skipInvokeExpr = true;

        if(stmt instanceof JReturnStmt && (preInvokeNode.type==RecordType.Assignment 
            // || preInvokeNode.type==RecordType.Return
          )){
          JReturnStmt rtStmt=(JReturnStmt) stmt;
          preInvokeNode.type=RecordType.Other;

          Value rtv=rtStmt.getOp();
          assert (rtv instanceof Immediate);

          if(rtv instanceof Constant){
            JAssignStmt preStmt=(JAssignStmt) preInvokeNode.stmt;

            Value lhs=preStmt.getLeftOp();

            preInvokeNode.symStore=putIntoImmutableMap(preInvokeNode.symStore, lhs, new ExprBox(rtv));

            preInvokeNode.note=String.format("-- Assignment");
          }
          else if(symStore0.containsKey(rtv)){
            
            ExprBox rr=RhsRewrite.rewriteRHS(rtv, symStore0);

            JAssignStmt preStmt=(JAssignStmt) preInvokeNode.stmt;

            Value lhs=preStmt.getLeftOp();
            
            if(lhs instanceof Local){
              // ValueRewrite nvr= new ValueRewrite(rr.str, preStmt.getRightOp());
              // preInvokeNode.symStore.put((Local) lhs, nvr);

              preInvokeNode.symStore=putIntoImmutableMap(preInvokeNode.symStore, lhs, rr);
            }
            else if(lhs instanceof JInstanceFieldRef){
              // JInstanceFieldRef lhs1=(JInstanceFieldRef) lhs;
              // ValueRewrite nvr= new ValueRewrite(rr.str, preStmt.getRightOp());
              // preInvokeNode.symStore.put((Local) lhs1.getBase(), nvr);

              preInvokeNode.symStore=putIntoImmutableMap(preInvokeNode.symStore, lhs, rr);
            }

            preInvokeNode.note=String.format("-- Assignment");
          }
        }

        currPath.addToPath(preInvokeNode);
        cachePath(currPath);
        currPath = null;
      }

    }
    else{
      HashSet<Stmt> headerBlockStmts = this.bodyToLoopInfoCache.get(body).headerBlockStmts;
      
      for(Unit succU: succList){
        Stmt succ=(Stmt) succU;

        /// DONE: no handle exception edges

        PathRecord outNode = new PathRecord(lastNode);
        outNode.stmt = succ;
        int numOccur=numOccurence(succ, currPath.path)+1;
        int maxOccur=headerBlockStmts.contains(succ)?MethodLoopInfo.maxOccurForHeader:1;
        outNode.numOccur=numOccur;
        outNode.maxOccur=maxOccur;
        outNode.type = decideRecordType(succ, numOccur>maxOccur, currPath.path);
        outNode.note = "";
        outNode.symStore=newFlowSet;

        PathConstraint pathOut=new PathConstraint(currPath);
        pathOut.addToPath(outNode);

        cacheOrDisgardPath(pathOut);
      }

      currPath=null;
    }
  }

  public boolean checkValidHandler(ArrayList<Type> exceptionStack, JIdentityStmt stmt) {
    Type throwType=exceptionStack.get(exceptionStack.size()-1);
    Type catchType=stmt.getLeftOp().getType();

    assert(throwType instanceof RefType);
    RefType r1=(RefType) throwType;
    SootClass t1=r1.getSootClass();

    assert(catchType instanceof RefType);
    RefType r2=(RefType) catchType;
    SootClass t2=r2.getSootClass();

    return Scene.v().getActiveHierarchy().isClassSubclassOfIncluding(t1, t2);
  }

  public void handleIdentity() {
    PathRecord lastNode = (PathRecord) currPath.path.get(currPath.path.size() - 1);
    JIdentityStmt stmt=(JIdentityStmt) lastNode.stmt;

    Value rhs=stmt.getRightOp();
    Value lhs=stmt.getLeftOp();
    assert(rhs instanceof IdentityRef);
    
    if(rhs instanceof ParameterRef){
      // if(lastNode.symStore.containsKey(lhs)){
      //   lastNode.type=RecordType.ParamUse;

      //   if(lastNode.symStore.get(lhs).str==null){
      //     ParameterRef pRef=(ParameterRef) rhs;

      //     String str=String.format("{%s}", paramNames.get(pRef.getIndex()));

      //     lastNode.note=String.format("-- End Point Param %s", str);

      //     ValueRewrite rr=lastNode.symStore.get(lhs);
      //     rr.v=rhs;
      //     rr.str=str;
      //   }
      //   else{
      //     ValueRewrite rr=lastNode.symStore.get(lhs);
      //     lastNode.note=rr.str;
      //   }
      // }
      // ParameterRef pRef=(ParameterRef) rhs;
      // String str=String.format("@parameter%d", pRef.getIndex());
      // Local intermediatLocal=new JimpleLocal(str, lhs.getType());
      // lastNode.type=RecordType.ParamUse;
      // lastNode.symStore.put(intermediatLocal, rhs);
      // lastNode.symStore.put(lhs, intermediatLocal);
      
      
      expandOnFirstSucc(lastNode);
    }
    else if(rhs instanceof JCaughtExceptionRef){
      /// DONE: exception handler

      lastNode.type=RecordType.ExceptionCatch;
      lastNode.note="-- Exception Caught";

      ArrayList<Type> exceptionStack=lastNode.exceptionStack;

      // incorrect handler
      if(!checkValidHandler(exceptionStack, stmt)){
        currPath=null;
        return;
      }

      ArrayList<Type> newExceptionStack=new ArrayList<>(exceptionStack);
      newExceptionStack.remove(newExceptionStack.size()-1);
      lastNode.exceptionStack=newExceptionStack;
      
      expandSuccs(lastNode);
    }
    // rhs instanceof ThisRef
    // Left empty for future uses
    else{
      expandOnFirstSucc(lastNode);
    }
  }

  // expand on the first (and the only) succ of stmt
  // add new path to cachedPaths
  // copy symStore, callStack and exceptionStack from inNode
  // DON'T use this when an exception edge is possible
  public void expandOnFirstSucc(PathRecord inNode) {
    Stmt stmt = inNode.stmt;

    Body body=icfg.getBodyOf(stmt);
    DirectedGraph<Unit> d_graph = icfg.getOrCreateUnitGraph(body);
    assert (d_graph instanceof ExceptionalUnitGraph);
    ExceptionalUnitGraph graph = (ExceptionalUnitGraph) d_graph;

    List<Unit> succList=graph.getUnexceptionalSuccsOf(stmt);
    assert (succList.size()==1);

    HashSet<Stmt> headerBlockStmts = this.bodyToLoopInfoCache.get(body).headerBlockStmts;

    Stmt succ=(Stmt) succList.get(0);
    PathRecord fallThruNode = new PathRecord(inNode);
    fallThruNode.stmt = succ;
    int numOccur=numOccurence(succ, currPath)+1;
    int maxOccur=headerBlockStmts.contains(succ)?MethodLoopInfo.maxOccurForHeader:1;
    fallThruNode.numOccur=numOccur;
    fallThruNode.maxOccur=maxOccur;
    fallThruNode.type = decideRecordType(succ, numOccur>maxOccur, currPath);
    fallThruNode.note = "";
    currPath.addToPath(fallThruNode);

    cacheOrDisgardPath(currPath);
    currPath=null;
  }

  // append target to currPath
  public void handleGoto() {
    PathRecord lastNode = (PathRecord) currPath.path.get(currPath.path.size() - 1);
    JGotoStmt stmt = (JGotoStmt) lastNode.stmt;

    Stmt tgt = (Stmt) stmt.getTarget();
    assert (tgt != null);

    Body body=icfg.getBodyOf(stmt);
    HashSet<Stmt> headerBlockStmts = this.bodyToLoopInfoCache.get(body).headerBlockStmts;

    PathRecord fallThruNode = new PathRecord(lastNode);
    fallThruNode.stmt = tgt;
    int numOccur=numOccurence(tgt, currPath)+1;
    int maxOccur=headerBlockStmts.contains(tgt)?MethodLoopInfo.maxOccurForHeader:1;
    fallThruNode.numOccur=numOccur;
    fallThruNode.maxOccur=maxOccur;
    fallThruNode.type = decideRecordType(tgt, numOccur>maxOccur, currPath);
    // fallThruNode.note="";

    currPath.addToPath(fallThruNode);

    cacheOrDisgardPath(currPath);

    currPath=null;
  }

  // adding both IF and ELSE branches to cachedPaths
  public void handleIf(){
    PathRecord lastNode = (PathRecord) currPath.path.get(currPath.path.size() - 1);

    JIfStmt stmt=(JIfStmt)lastNode.stmt;

    Stmt tgt=stmt.getTarget();

    assert(tgt!=null);

    Body body=icfg.getBodyOf(stmt);
    DirectedGraph<Unit> d_graph = icfg.getOrCreateUnitGraph(body);
    assert (d_graph instanceof ExceptionalUnitGraph);
    ExceptionalUnitGraph graph = (ExceptionalUnitGraph) d_graph;

    List<Unit> succList= graph.getUnexceptionalSuccsOf(stmt);

    /// DONE: handle exceptional edges when necessary
    // no exception can be thrown at If stmt

    /// DONE: check if this step filters out tgt and exceptional edges
    // exceptional succs filter out above with getUnexceptionalSuccsOf
    succList.removeIf(succ -> succ.equals(tgt));

    assert (succList.size()==1);

    HashSet<Stmt> headerBlockStmts = this.bodyToLoopInfoCache.get(body).headerBlockStmts;
    boolean isInLoopHeader=headerBlockStmts.contains(stmt);

    assert (stmt.getCondition() instanceof ConditionExpr);
    Value condValue=stmt.getCondition();
    Value condRewriten=RhsRewrite.rewriteRHS(condValue, lastNode.symStore).getValue();
    // String condString=ValueRewrite.rewriteValueWith(condValue, lastNode.symStore, body);
    String condString=condValue.toString();
    ConditionPred condElse = new ConditionPred(condRewriten, false, condString, isInLoopHeader);

    for(ValueBox ub:condValue.getUseBoxes()){
      Value u=ub.getValue();
      if(u instanceof StaticFieldRef){
        StaticFieldRef u1= (StaticFieldRef)u;
        this.globalVarRead.add(u1.getField());
      }
    }

    ConstantComp checkElseCmp=ConditionPred.checkConstantComparison(condElse);
    if(checkElseCmp!=ConstantComp.EvalFalse){


      // create and cache ELSE branch path
      Stmt succ=(Stmt) succList.get(0);
      PathConstraint pathElse=new PathConstraint(currPath);
      PathRecord elseNode=new PathRecord(lastNode);
      elseNode.note=String.format("!(%s)", condString);
      pathElse.path.set(pathElse.path.size()-1, elseNode);

      boolean pathOK=true;
      if(checkElseCmp==ConstantComp.NonConstant){
        pathElse.addToCond(condElse);

        if(pathElse.conds.size()%4==0){
          PathFeasibility feasibility=new PathFeasibility(pathElse);
          pathOK= feasibility.check();
        }
      }

      if(pathOK){
        PathRecord fallThruNode=new PathRecord(elseNode);
        fallThruNode.stmt=succ;
        int numOccur1=numOccurence(succ, pathElse)+1;
        int maxOccur1=headerBlockStmts.contains(succ)?MethodLoopInfo.maxOccurForHeader:1;
        fallThruNode.numOccur=numOccur1;
        fallThruNode.maxOccur=maxOccur1;
        fallThruNode.type=decideRecordType(succ, numOccur1>maxOccur1, pathElse);
        fallThruNode.note="";
        pathElse.addToPath(fallThruNode);
  
        cacheOrDisgardPath(pathElse);
      }
    }
    

    ConditionPred condIf = new ConditionPred(condRewriten, true, condString, isInLoopHeader);

    ConstantComp checkIfCmp;
    switch (checkElseCmp) {
      case NonConstant:
        checkIfCmp=ConstantComp.NonConstant;  
        break;
      case EvalFalse:
        checkIfCmp=ConstantComp.EvalTrue;
        break;
      case EvalTrue:
        checkIfCmp=ConstantComp.EvalFalse;
        break;
      default:
        throw new RuntimeException("ConstantComp not caught");
    }

    if(checkIfCmp!=ConstantComp.EvalFalse){

      // add IF target to currPath
      lastNode.note=String.format("%s", condString);

      boolean pathOK=true;
      if(checkElseCmp==ConstantComp.NonConstant){
        currPath.addToCond(condIf);

        if(currPath.conds.size()%4==0){
          PathFeasibility feasibility=new PathFeasibility(currPath);
          pathOK= feasibility.check();
        }
      }

      if(pathOK){
        PathRecord branchNode=new PathRecord(lastNode);
        branchNode.stmt=tgt;
        int numOccur2=numOccurence(tgt, currPath)+1;
        int maxOccur2=headerBlockStmts.contains(tgt)?MethodLoopInfo.maxOccurForHeader:1;
        branchNode.numOccur=numOccur2;
        branchNode.maxOccur=maxOccur2;
        branchNode.type=decideRecordType(tgt, numOccur2>maxOccur2, currPath);
        branchNode.note="";
        currPath.addToPath(branchNode);

        cacheOrDisgardPath(currPath);        
      }

    }

    currPath=null;
  }

  public void initMethodEntry() {
    SootMethod m=this.method;
    ArrayList<ParamInfo> params=new ArrayList<>();

    for(int i=0;i<m.getParameterCount();++i){
      if(this.paramNames.containsKey(i)){
        params.add(new ParamInfo(i, new EndPointParameter(this.paramNames.get(i), true, i, m.getParameterType(i))));
      }
      else{
        params.add(new ParamInfo(i, new EndPointParameter(String.format("non_EPP#%d",i), false, i, m.getParameterType(i))));
      }
    }

    // add Method Entry to currPath
    currPath.addToPath(new MethodEntryRecord(m, params, new ArrayList<>()));
  }

  boolean decideInitInvoke(Stmt stmt){
    if(stmt instanceof JInvokeStmt){
      JInvokeStmt invokeStmt=(JInvokeStmt) stmt;
      InvokeExpr expr=invokeStmt.getInvokeExpr();
      if(expr instanceof JSpecialInvokeExpr){
        if(expr.getMethod().getName().equals("<init>")){
          return true;
        }
      }
    }

    return false;
  }

  boolean _decideInvokeExpr(Stmt stmt){
    if (stmt.containsInvokeExpr()) {
      ArrayList<SootMethod> callees = this.getCalleesAt(stmt);

      return callees.stream().anyMatch(m -> checkAppClass(m));
    }

    return false;
  }

  int numOccurence(Stmt stmt, PathConstraint path){
    return numOccurence(stmt, path.path);
  }

  /// DONE: somehow buggy. It doesn't detect A->B->A
  int numOccurence(Stmt stmt, ArrayList<PathRecordBase> path){
    int len=path.size();

    PathRecordBase lastNode=path.get(len-1);
    int stackH=lastNode.callStack.size();
    if(lastNode.type==RecordType.MethodEntry){
      stackH=stackH + 1;
    }

    int i=len-1;
    while(i>=0){
      
      int h=path.get(i).callStack.size();
      while(h>stackH){
        i=i-1;
        h=path.get(i).callStack.size();
      }

      PathRecordBase rec=path.get(i);

      if(rec instanceof PathRecord){
        PathRecord rec1=(PathRecord) rec;
        if(rec1.stmt.equals(stmt)){
          return rec1.numOccur;
        }
      }

      stackH=rec.callStack.size();

      --i;
    }

    return 0;
  }

  PathRecord.RecordType decideRecordType(Stmt stmt, boolean isBackEdge, PathConstraint path) {
    return decideRecordType(stmt, isBackEdge, path.path);
  }

  PathRecord.RecordType decideRecordType(Stmt stmt, boolean isBackEdge, ArrayList<PathRecordBase> path) {
    if (isBackEdge) {
      return RecordType.BackEdge;
    }

    
    if (stmt instanceof JIfStmt) {
      return RecordType.If;
    }

    if (stmt instanceof JThrowStmt) {
      return RecordType.Throw;
    }

    if (stmt instanceof JGotoStmt) {
      return RecordType.Goto;
    }

    if (stmt instanceof JIdentityStmt) {
      return RecordType.Identity;
    }

    if (stmt instanceof JAssignStmt) {
      return RecordType.Assignment;
    }

    if(stmt instanceof SwitchStmt){
      return RecordType.Switch;
    }

    if(stmt instanceof InvokeStmt){

      if(isInvokeToInit(stmt)){
        return RecordType.NewInvoke;
      }

      return RecordType.InvokeStmt;
    }


    return RecordType.Other;
  }

  boolean isInvokeToInit(Stmt stmt){
    if(stmt instanceof JInvokeStmt){
      JInvokeStmt invokeStmt=(JInvokeStmt) stmt;
      InvokeExpr expr = invokeStmt.getInvokeExpr();
      if(expr instanceof JSpecialInvokeExpr && expr.getMethodRef().getName().equals("<init>")){
        return true;
      }
    }

    return false;
  }

  // adding to cachedPaths, set currPath to null
  public void handleMethodEntry() {
    MethodEntryRecord lastNode = (MethodEntryRecord) currPath.path.get(currPath.path.size() - 1);
    SootMethod m = lastNode.method;
    ArrayList<ParamInfo> params = lastNode.paramInfo;

    if(!(m.hasActiveBody())){
      currPath=null;
      return;
    }
    JimpleBody body = (JimpleBody) m.getActiveBody();
    DirectedGraph<Unit> d_graph = icfg.getOrCreateUnitGraph(body);
    assert (d_graph instanceof ExceptionalUnitGraph);
    ExceptionalUnitGraph graph = (ExceptionalUnitGraph) d_graph;

    MethodLoopInfo loopInfo=this.bodyToLoopInfoCache.computeIfAbsent(body, b -> new MethodLoopInfo(b, graph));
    assert(loopInfo!=null);

    ImmutableMap<Value, ExprBox> symStore = ImmutableMap.of();

    
    for(Unit u:body.getUnits()){
      //@param-assignment statements should precede all non-identity statements (validated by IdentityStatementsValidator)
      if (u instanceof JIdentityStmt) {
        JIdentityStmt is = (JIdentityStmt) u;
        Value rhs=is.getRightOp();
        if (rhs instanceof ParameterRef) {
          ParameterRef pr = (ParameterRef) rhs;
          Local lhs=(Local) is.getLeftOp();

          // Iterator<ParamInfo> it= params.stream().filter(info->info.idx==pr.getIndex()).iterator();

          // if(it.hasNext()){
          //   ValueRewrite rr=new ValueRewrite(it.next().rr.str, rhs);
          //   symStore.put(lhs, rr);
          // }

          Value paramV=params.get(pr.getIndex()).rr;

          if(paramV instanceof EndPointParameter){
            EndPointParameter epp=(EndPointParameter) paramV;
            if(epp.getName().isEmpty()){
              String nameLHS=lhs.getName();

              assert nameLHS.isEmpty()==false;

              epp.setName(nameLHS);

              this.paramNames.put(epp.idx, nameLHS);
            }
          }

          symStore=putIntoImmutableMap(symStore, lhs, new ExprBox(paramV));
          
        }
      }
      else{
        break;
      }
    }
    

    List<Unit> heads = graph.getHeads();

    // Unit h=body.getFirstNonIdentityStmt();

    ArrayList<SootMethod> callStack = new ArrayList<>(lastNode.callStack);
    callStack.add(m);

    ArrayList<Type> exceptionStack=new ArrayList<>();

    HashSet<Stmt> headerBlockStmts = this.bodyToLoopInfoCache.get(body).headerBlockStmts;

    for (Unit h : heads) {
      Stmt stmt = (Stmt) h;

      int numOccur=numOccurence(stmt, currPath)+1;
      int maxOccur=headerBlockStmts.contains(stmt)?MethodLoopInfo.maxOccurForHeader:1;

      RecordType type = decideRecordType(stmt, numOccur>maxOccur, currPath);

      PathConstraint newPath = new PathConstraint(currPath);
      newPath.addToPath(new PathRecord(stmt, type, null, symStore, callStack, exceptionStack, maxOccur, numOccur));

      cacheOrDisgardPath(newPath);

    }

    currPath = null;
  }

  Pair<Integer, JsonElement> genResponseCodeAndSchema(PathConstraint path){
    Value rtv=path.returnValue;

    if(rtv==null || ConditionPred.evalToNullConstant(rtv)==ConstantComp.EvalTrue){
      return Pair.of(this.frameworkData.nullReturnCode, null);
    }

    int statusCode=this.EPInfo.responseStatus;

    ArrayList<ValueBox> vbs= new ArrayList<>(rtv.getUseBoxes());
    if(rtv instanceof InvokeExpr){
      vbs.add(Grimp.v().newExprBox(rtv));
    }

    for(ValueBox vb: vbs){
      Value v=vb.getValue();

      if(v instanceof InvokeExpr){
        InvokeExpr inv=(InvokeExpr) v;
        SootMethodRef r= inv.getMethodRef();
        for(ResponseBuilderInfo builderInfo: this.frameworkData.responseBuilders){
          if(r.getSubSignature().getString().equals(builderInfo.subSignature)){
            
            Integer code0=builderInfo.extractStatusCode(inv, this.frameworkData);
            JsonElement sch0=builderInfo.extractSchema(inv, this.frameworkData);

            if(code0!=null){
              statusCode=code0;
            }

            return Pair.of(statusCode, sch0);
          }
        }
      }
    }

    JsonElement schema=ResponseSchemaGen.toResponseSchema(rtv.getType(), this.frameworkData.packagePrefix);
    return Pair.of(statusCode, schema);
  }
}
