package org.rest.Respector.archive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BriefUnitPrinter;
import soot.Local;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Unit;
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
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;

public class MyAppOld extends SceneTransformer {
  public ArrayList<Pair<SootMethod, ArrayList<Integer>>> endpointsWithParam;
  public HashMap<Body,BriefUnitPrinter> printerSet;
  JimpleBasedInterproceduralCFG icfg;

  public MyAppOld(ArrayList<Pair<SootMethod, ArrayList<Integer>>> listEndpointsWithParam) {
    this.endpointsWithParam = listEndpointsWithParam;
  }

  @Override
  protected void internalTransform(String phaseName, Map<String, String> options) {

    icfg = new JimpleBasedInterproceduralCFG();
    printerSet=new HashMap<Body,BriefUnitPrinter>();

    for(Pair<SootMethod, ArrayList<Integer>> pairMP : endpointsWithParam){
      SootMethod m = pairMP.getO1();
      ArrayList<Integer> paramIdx = pairMP.getO2();


      ArrayList<ArrayList<Pair<Stmt, String>>> traces= traverseMethod(m, paramIdx, new ArrayList<>());

      for(ArrayList<Pair<Stmt, String>> p: traces){
        printPath(p);
      }
    }

    // pathPass0(endpointsWithParam);
  }

  public ArrayList<ArrayList<Pair<Stmt, String>>> traverseMethod(SootMethod m, ArrayList<Integer> paramIdx, ArrayList<SootMethod> callStack) {
    JimpleBody body = (JimpleBody) m.getActiveBody();
    UnitGraph graph = new ExceptionalUnitGraph(body);

    List<Local> paramLocals = body.getParameterLocals();

    assert (paramLocals.size() >= paramIdx.size());
    assert (paramLocals instanceof ArrayList);

    HashSet<Local> flowSet = new HashSet<>();

    for (int idx : paramIdx) {
      Local lhs = paramLocals.get(idx);
      if (lhs != null) {
        flowSet.add(lhs);
      }
    }

    List<Unit> heads = graph.getHeads();

    ArrayList<SootMethod> callStack1=new ArrayList<>(callStack);

    // icfg.getStartPointsOf(m);

    ArrayList<ArrayList<Pair<Stmt, String>>> paths=new ArrayList<>();

    for (Unit h : heads) {

      Stmt stmt = (Stmt) h;
      ArrayList<ArrayList<Pair<Stmt, String>>> t=traverseStmt(stmt, new ArrayList<>(), flowSet, callStack1);

      // for(Pair<ArrayList<Pair<Stmt, String>>, HashSet<Local>> p :t){
      //   paths.add(p.getO1());
      // }
      paths.addAll(t);
    }

    return paths;
  }

  public ArrayList<ArrayList<Pair<Stmt, String>>> traverseStmt(final Stmt currStmt, final ArrayList<Pair<Stmt, String>> pathPrev, HashSet<Local> flowSet, final ArrayList<SootMethod> callStack){
  
    JimpleBody body=(JimpleBody)icfg.getBodyOf(currStmt);

    if(body==null){
      throw new RuntimeException("null Body?!");
    }

    Stmt stmt=currStmt;
    ArrayList<Pair<Stmt, String>> path=new ArrayList<>(pathPrev);

    ArrayList<ArrayList<Pair<Stmt, String>>> traces=new ArrayList<>();

    while (true) {

      Stmt stmt2=stmt;
      if(path.stream().anyMatch(p -> p.getO1().equals(stmt2))){
        // path.add(new Pair<>(stmt,"backedge"));
        // printPath(pathPrev);
        return traces;
      }

      if(stmt.containsInvokeExpr()){
        // JInvokeStmt s0=(JInvokeStmt)stmt;
        Collection<SootMethod> callees=icfg.getCalleesOfCallAt(stmt);

        callees.removeIf(m -> m.getName().equals("<clinit>"));
        
        InvokeExpr expr=stmt.getInvokeExpr();

        List<Value> exprArgs=expr.getArgs();

        ArrayList<Integer> paramIdx=new ArrayList<>();
        
        {
          int i=0;
          for(Value exprArg:exprArgs){
            if(flowSet.contains(exprArg)){
              paramIdx.add(i);
            }

            ++i;
          }          
        }

        ArrayList<ArrayList<Pair<Stmt, String>>> allSub=new ArrayList<>();
        if(!paramIdx.isEmpty()){
          path.add(new Pair<>(stmt, String.format(">> Invoke to %d impl, param %s in flowSet", callees.size(), paramIdx.toString())));

          for(SootMethod callee:callees){
            // if(callee.getName().equals("<clinit>")){
            //   continue;
            // }

            ArrayList<ArrayList<Pair<Stmt, String>>> t=traverseMethod(callee, paramIdx, callStack);

            allSub.addAll(t);

          }
        }
        // else{
        //   path.add(new Pair<>(stmt, ""));
        // }

        for(ArrayList<Pair<Stmt, String>> p: allSub){

          List<Unit> succList= icfg.getSuccsOf(stmt);

          if(succList.isEmpty()){
            // if(callStack.isEmpty()){
            //   printPath(path);
            // }
            ArrayList<Pair<Stmt, String>> p1=new ArrayList<>(path);
            p1.addAll(p);

            traces.add(p1);
            // return traces;
          }
          else{
            ArrayList<Pair<Stmt, String>> p1=new ArrayList<>(path);
            p1.addAll(p);

            for(Unit succ: succList){
              Stmt succStmt=(Stmt) succ;

              // if(succStmt instanceof JIdentityStmt && ((JIdentityStmt)succStmt).getRightOp() instanceof JCaughtExceptionRef){
              //   continue;
              // }

              ArrayList<ArrayList<Pair<Stmt, String>>> t1=traverseStmt(succStmt, p1, flowSet, callStack);
              traces.addAll(t1);

            }
            // return traces;
          }
        }
        
      }
    
      if(stmt instanceof JIfStmt){
        JIfStmt s0=(JIfStmt) stmt;

        Stmt tgt=s0.getTarget();

        if(tgt!=null){
          ArrayList<Pair<Stmt, String>> path1=new ArrayList<>(path);
          path1.add(new Pair<>(stmt, "-- IF branch: "+s0.getCondition().toString()));

          ArrayList<ArrayList<Pair<Stmt, String>>> t=traverseStmt(tgt, path1, flowSet, callStack);
          traces.addAll(t);

          // return;
        }

        List<Unit> succList= icfg.getSuccsOf(s0);

        // if(succList.size()!=2){
        //   throw new RuntimeException("if statment has more than 2 successors?");
        // }

        succList.removeIf(s -> s.equals(tgt));

        assert (succList.size()==1);

        // ArrayList<Pair<Stmt, String>> path=new ArrayList<>(pathPrev);
        path.add(new Pair<>(stmt, "-- ELSE branch: !("+s0.getCondition().toString()+")"));
        
        Stmt succStmt=(Stmt) succList.get(0);
        // traverseStmt(succStmt, path, flowSet);
        
        stmt=succStmt;
        continue;

      }
      // else if(stmt instanceof JThrowStmt){
      //   path.add(new Pair<>(stmt, "-- Throw"));

      //   // traces.add(path);

      //   return traces;
      // }
      else if(stmt instanceof JGotoStmt){
        JGotoStmt s0=(JGotoStmt) stmt;

        Stmt tgt=(Stmt)s0.getTarget();

        assert(tgt!=null);
      
        // ArrayList<Pair<Stmt, String>> path=new ArrayList<>(pathPrev);
        path.add(new Pair<>(stmt, "-- Goto"));

        // traverseStmt(tgt, path, flowSet);
        stmt=tgt;
        continue;

      }
      else if(stmt instanceof JIdentityStmt){
        JIdentityStmt s0=(JIdentityStmt) stmt;

        Value rhs=s0.getRightOp();

        if(rhs instanceof JCaughtExceptionRef){
          path.add(new Pair<>(stmt, "-- Exception Caught"));

          List<Unit> succList= icfg.getSuccsOf(stmt);
          assert (succList.size()==1);

          Stmt succStmt=(Stmt) succList.get(0);
          stmt=succStmt;

          // traverseStmt(succStmt, path, flowSet);
          // return;

          continue;
        }
        else{
          // ArrayList<Pair<Stmt, String>> path=new ArrayList<>(pathPrev);
          if(flowSet.contains(s0.getLeftOp())){
            path.add(new Pair<>(stmt, "-- Endpoint param"));

          }
          else{
            path.add(new Pair<>(stmt, ""));
          }
          
          List<Unit> succList= icfg.getSuccsOf(stmt);
          assert (succList.size()==1);

          Stmt succStmt=(Stmt) succList.get(0);
          stmt=succStmt;

          // traverseStmt(succStmt, path, flowSet);
          // return;

          continue;
        }

        
      }
      else if(stmt instanceof JAssignStmt){
        JAssignStmt s0=(JAssignStmt) stmt;

        List<ValueBox> useVBs=s0.getUseBoxes();

        boolean inFlowSet=false;
        for(ValueBox useVB: useVBs){
          Value v=useVB.getValue();
          if(v instanceof Local && flowSet.contains((Local)v)){
            inFlowSet=true;
            break;
          }
        }

        if(inFlowSet){
          path.add(new Pair<>(stmt, "-- Assignment from flowSet"));

          Value lhs=s0.getLeftOp();

          flowSet=new HashSet<>(flowSet);

          if(lhs instanceof Local){
            flowSet.add((Local) lhs);
          }
          else if(lhs instanceof JInstanceFieldRef){
            JInstanceFieldRef lhs1=(JInstanceFieldRef) lhs;

            flowSet.add((Local) lhs1.getBase());
          }
        }
        else{
          path.add(new Pair<>(stmt, ""));
        }

        List<Unit> succList= icfg.getSuccsOf(s0);

        if(succList.isEmpty()){
          // if(callStack.isEmpty()){
          //   printPath(path);
          // }

          traces.add(path);
          return traces;
        }
        else if(succList.size()==1){
          stmt=(Stmt)succList.get(0);

          continue;
        }
        else{
          for(Unit succ: succList){
            Stmt succStmt=(Stmt) succ;

            // if(succStmt instanceof JIdentityStmt && ((JIdentityStmt)succStmt).getRightOp() instanceof JCaughtExceptionRef){
            //   continue;
            // }
            ArrayList<ArrayList<Pair<Stmt, String>>> t=traverseStmt(succStmt, path, flowSet, callStack);
            traces.addAll(t);

          }
          return traces;
        }

      }
      else{
        path.add(new Pair<>(stmt, ""));

        List<Unit> succList= icfg.getSuccsOf(stmt);

        if(succList.isEmpty()){
          // if(callStack.isEmpty()){
          //   printPath(path);
          // }
          traces.add(path);
          return traces;
        }
        else if(succList.size()==1){
          stmt=(Stmt)succList.get(0);

          continue;
        }
        else{
          for(Unit succ: succList){
            Stmt succStmt=(Stmt) succ;

            // if(succStmt instanceof JIdentityStmt && ((JIdentityStmt)succStmt).getRightOp() instanceof JCaughtExceptionRef){
            //   continue;
            // }

            ArrayList<ArrayList<Pair<Stmt, String>>> t=traverseStmt(succStmt, path, flowSet, callStack);
            traces.addAll(t);

          }
          return traces;
        }
      }
    }
  }

  public void printPath(ArrayList<Pair<Stmt, String>> path){
    printPath(path,0);
  }

  public void printPath(ArrayList<Pair<Stmt, String>> path, int indent){
    assert(path.size()>0);
    SootMethod m0=icfg.getMethodOf(path.get(0).getO1());
    System.out.println(m0!=null?m0.getName():"unknown");

    for(Pair<Stmt, String> p:path){
      Stmt s=p.getO1();
      String note=p.getO2();

      Body b=icfg.getBodyOf(s);

      BriefUnitPrinter pt=printerSet.get(b);

      if(pt==null){
        pt=new BriefUnitPrinter(b);
        pt.noIndent();
        printerSet.put(b, pt);
      }

      s.toString(pt);
      System.out.print(String.format("%s%d\t%s        %s\n", " ".repeat(indent), s.getJavaSourceStartLineNumber(), pt.toString(), note));
    }

    System.out.print("----------------------\n\n");
  }

  static public void pathPass0(ArrayList<Pair<SootMethod, ArrayList<Integer>>> endpointsWithParam) {

    for (Pair<SootMethod, ArrayList<Integer>> pairMP : endpointsWithParam) {
      SootMethod m = pairMP.getO1();
      ArrayList<Integer> paramIdx = pairMP.getO2();

      JimpleBody body = (JimpleBody) m.getActiveBody();
      UnitGraph graph = new ExceptionalUnitGraph(body);

      List<Local> param_locals = body.getParameterLocals();

      SimpleLocalDefs sld = new SimpleLocalDefs(graph);

      SimpleLocalUses slu = new SimpleLocalUses(graph, sld);

      ArrayList<Unit> uses_param = new ArrayList<Unit>();

      for (Unit u : body.getUnits()) {
        if (!(u instanceof IdentityStmt)) {
          continue;
        }

        List<ValueBox> vbs = u.getDefBoxes();

        for (ValueBox vb : vbs) {
          Value v = vb.getValue();

          if (v instanceof Local) {
            if (param_locals.contains(v)) {
              uses_param.add(u);

              break;
            }
          }
        }

      }

      // for(Unit u:uses_param){
      // System.out.println(String.format("%s:%s",m.getName(),u.toString()));
      // for(UnitValueBoxPair p:slu.getUsesOf(u)){
      // System.out.println(String.format("\t%s:%d",
      // p.getUnit().toString(),p.getUnit().getJavaSourceStartLineNumber()));
      // }
      // }

      // TreeSet<Unit> visited=new TreeSet<>();

      ArrayList<ArrayList<Unit>> paths = new ArrayList<>();

      for (Unit u : uses_param) {
        // ArrayList<Unit> a=new ArrayList<>();
        // a.add(u);
        ArrayList<Unit> p0 = new ArrayList<>();

        p0.add(u);
        // visited.add(u);

        paths.addAll(recur(u, null, slu, p0, graph));

      }

      System.out.println(String.format("%s:%d", m.getName(), paths.size()));

      BriefUnitPrinter pt = new BriefUnitPrinter(body);
      pt.noIndent();
      for (ArrayList<Unit> p : paths) {
        for (Unit u : p) {
          u.toString(pt);
          System.out.println(String.format("\t%s  %d", pt.toString(), u.getJavaSourceStartLineNumber()));
        }
        System.out.println("---------------------");
      }
    }
  }

  static public ArrayList<ArrayList<Unit>> recur_method(SootMethod m, int idx, ArrayList<Unit> path) {
    ArrayList<ArrayList<Unit>> paths = new ArrayList<>();

    if (m.isPhantom()) {
      return paths;
    }

    JimpleBody body = (JimpleBody) m.getActiveBody();
    UnitGraph graph = new ExceptionalUnitGraph(body);

    Local param_local = body.getParameterLocal(idx);

    SimpleLocalDefs sld = new SimpleLocalDefs(graph);

    SimpleLocalUses slu = new SimpleLocalUses(graph, sld);

    ArrayList<Unit> uses_param = new ArrayList<Unit>();

    for (Unit u : body.getUnits()) {
      List<ValueBox> vbs = u.getDefBoxes();

      for (ValueBox vb : vbs) {
        Value v = vb.getValue();

        if (v instanceof Local) {
          if (v.equals(param_local)) {
            uses_param.add(u);
          }
        }
      }
    }

    for (Unit u : uses_param) {
      // ArrayList<Unit> a=new ArrayList<>();
      // a.add(u);
      ArrayList<Unit> p0 = new ArrayList<>(path);

      p0.add(u);
      // visited.add(u);

      paths.addAll(recur(u, null, slu, p0, graph));

    }

    return paths;
  }

  static public ArrayList<ArrayList<Unit>> recur(Unit u, ValueBox vb, SimpleLocalUses slu, ArrayList<Unit> path, UnitGraph g) {
    ArrayList<ArrayList<Unit>> p1 = new ArrayList<>();

    List<UnitValueBoxPair> uses_list = slu.getUsesOf(u);

    for (UnitValueBoxPair p : uses_list) {
      Unit u1 = p.getUnit();
      ValueBox vb1 = p.getValueBox();

      if (path.contains(u1)) {
        continue;
      }

      ArrayList<Unit> p2 = new ArrayList<>(path);
      p2.add(u1);

      p1.addAll(recur(u1, vb1, slu, p2, g));
    }

    if (u instanceof JIfStmt) {
      JIfStmt stmt = (JIfStmt) u;
      ArrayList<Unit> p2 = new ArrayList<>(path);

      Stmt t = stmt.getTarget();
      p2.add(t);

      p1.addAll(recur(t, null, slu, p2, g));
    }

    if (u instanceof GotoStmt) {
      GotoStmt stmt = (GotoStmt) u;

      ArrayList<Unit> p2 = new ArrayList<>(path);

      Unit t = stmt.getTarget();

      p2.add(t);

      p1.addAll(recur(t, null, slu, p2, g));
    }

    // if(u instanceof InvokeStmt && vb!=null){
    // InvokeStmt stmt=(InvokeStmt) u;

    // ArrayList<Unit> p2=new ArrayList<>(path);

    // InvokeExpr expr=stmt.getInvokeExpr();

    // List<Value> args=expr.getArgs();

    // int len=args.size();

    // int idx=-1;

    // for(int i=0;i<len;++i){
    // Value v=args.get(i);

    // List<ValueBox> vbs=v.getUseBoxes();

    // if(vbs.contains(vb)){
    // idx=i;

    // break;
    // }
    // }
    // assert(idx!=-1);

    // SootMethod callee=expr.getMethod();

    // p1.addAll(recur_method(callee, idx, path));

    // }

    if (p1.isEmpty()) {
      p1.add(path);
    }

    return p1;
  }

}
