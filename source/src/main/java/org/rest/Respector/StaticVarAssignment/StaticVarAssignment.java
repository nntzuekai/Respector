package org.rest.Respector.StaticVarAssignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import soot.AmbiguousMethodException;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.Chain;
import soot.util.queue.QueueReader;

public class StaticVarAssignment {
  public HashMap<SootField, ArrayList<StaticExampleLoc>> result=new HashMap<>();
  public final CallGraph cg;

  public StaticVarAssignment(CallGraph cg, Set<SootField> globalVarRead){
    this.cg=cg;

    Chain<SootClass> appClasses = Scene.v().getApplicationClasses();

    ArrayList<SootMethod> clinitMethods=new ArrayList<>();
    for(SootClass cl: appClasses){
      try {
        SootMethod cCtr= cl.getMethodByNameUnsafe("<clinit>");

        if(cCtr!=null){
          clinitMethods.add(cCtr);
        }

      } catch (AmbiguousMethodException e) {
        throw e;
      }
    }

    ReachableMethods closure=new ReachableMethods(this.cg, clinitMethods);

    QueueReader<MethodOrMethodContext> qReader = closure.listener();
    closure.update();

    while(qReader.hasNext()){
      MethodOrMethodContext m0=qReader.next();
      SootMethod m=m0.method();

      if(!m.hasActiveBody()){
        continue;
      }

      for(Unit u: m.getActiveBody().getUnits()){
        assert u instanceof Stmt;

        if(u instanceof JAssignStmt){
          JAssignStmt stmt=(JAssignStmt) u;

          Value lhs= stmt.getLeftOp();

          if(lhs instanceof StaticFieldRef){
            StaticFieldRef ref=(StaticFieldRef) lhs;
            SootField f= ref.getField();

            if(globalVarRead.contains(f)){
              ArrayList<StaticExampleLoc> l = result.computeIfAbsent(f, e-> new ArrayList<>());
              StaticExampleLoc eg=new StaticExampleLoc(stmt.getRightOp(), m.getDeclaringClass().getFilePath(), stmt.getJavaSourceStartLineNumber());
              l.add(eg);
            }
          }
        }
      }
    }
  }
}
