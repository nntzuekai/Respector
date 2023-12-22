package org.rest.Respector.Simplification;

import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.tuple.Pair;
import org.rest.Respector.PathCondExtract.ConditionPred;

import com.microsoft.z3.ApplyResult;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Goal;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Tactic;

import soot.Value;
import soot.grimp.Grimp;
import soot.jimple.ConditionExpr;
import soot.jimple.IntConstant;

public class ClusterSimpl extends SootToZ3{
  public ArrayList<ConditionPred> output=new ArrayList<>();
  public ArrayList<BoolExpr> rawOutput=new ArrayList<>();

  private static Logger logger=LoggerFactory.getLogger(ClusterSimpl.class);

  @Deprecated
  public Pair<ArrayList<ConditionPred>, ArrayList<BoolExpr>> getOuotputAndCloseCtx() {
    Pair<ArrayList<ConditionPred>, ArrayList<BoolExpr>> rtv=Pair.of(this.output, this.rawOutput);
    this.output=null;
    this.rawOutput=null;
    this.ctx.close();
    return rtv;
  }



  public ClusterSimpl(HashSet<ConditionPred> cluster){
    super(cluster);

    Goal goal=ctx.mkGoal(false, false, false);
    for(Expr<BoolSort> e: modelExprs){
      goal.add(e);
    }

    Tactic t = ctx.andThen(ctx.mkTactic("simplify"), ctx.mkTactic("propagate-values"), ctx.mkTactic("propagate-ineqs"), ctx.mkTactic("ctx-solver-simplify"));

    ApplyResult ar= t.apply(goal);
    Goal[] subGoals = ar.getSubgoals();

    assert subGoals.length==1;

    
    for(BoolExpr expr: subGoals[0].getFormulas()){
      this.rawOutput.add(expr);

      try {
        Value v=z3ToSoot(expr);

        output.add(new ConditionPred(v, true, null, false));
      } catch (RuntimeException e) {
        logger.debug("Failed to convert to Z3: "+expr.toString());
      }
      
    }

    output.addAll(notConverted);
  }

  Value z3ToSoot(Expr expr){
    if(varMapRev.containsKey(expr)){
      return varMapRev.get(expr);
    }
    
    if(expr.isEq()){
      Value op1=z3ToSoot(expr.getArgs()[0]);
      Value op2=z3ToSoot(expr.getArgs()[1]);

      return Grimp.v().newEqExpr(op1, op2);
    }

    if(expr.isNot()){
      Value op1=z3ToSoot(expr.getArgs()[0]);

      return Grimp.v().newNegExpr(op1);
    }

    if(expr.isGE()){
      Value op1=z3ToSoot(expr.getArgs()[0]);
      Value op2=z3ToSoot(expr.getArgs()[1]);

      return Grimp.v().newGeExpr(op1, op2);
    }

    if(expr.isGT()){
      Value op1=z3ToSoot(expr.getArgs()[0]);
      Value op2=z3ToSoot(expr.getArgs()[1]);

      return Grimp.v().newGtExpr(op1, op2);
    }

    if(expr.isLE()){
      Value op1=z3ToSoot(expr.getArgs()[0]);
      Value op2=z3ToSoot(expr.getArgs()[1]);

      return Grimp.v().newLeExpr(op1, op2);
    }

    if(expr.isLT()){
      Value op1=z3ToSoot(expr.getArgs()[0]);
      Value op2=z3ToSoot(expr.getArgs()[1]);

      return Grimp.v().newLtExpr(op1, op2);
    }

    if(expr.isIntNum()){
      int i =((IntNum) expr).getInt();
      return IntConstant.v(i);
    }

    throw new RuntimeException("Expr not supported");
  }
}
