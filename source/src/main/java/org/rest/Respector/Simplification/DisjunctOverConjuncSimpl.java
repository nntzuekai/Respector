package org.rest.Respector.Simplification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.tuple.Pair;
import org.rest.Respector.PathCondExtract.ConditionPred;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class DisjunctOverConjuncSimpl extends SootToZ3 {
  private static Logger logger = LoggerFactory.getLogger(DisjunctOverConjuncSimpl.class);

  public ArrayList<BoolExpr> simplifiedGoals=new ArrayList<>();

  public DisjunctOverConjuncSimpl(Collection<ArrayList<ConditionPred>> disOfCon) {
    super(true);

    ArrayList<Expr<BoolSort>> z3Dis=new ArrayList<>();
    for(ArrayList<ConditionPred> con: disOfCon){
      ArrayList<Expr<BoolSort>> z3Con=new ArrayList<>();
      for(ConditionPred pred: con){
        if(pred.inLoopHeader){
          continue;
        }
        try{
          Expr<BoolSort> expr=toZ3Expr(pred.uniCond).simplify();
          z3Con.add(expr);
          ArrayList<Expr<BoolSort>> auxi = getAndClearAuxiliaryExprs();
          z3Con.addAll(auxi);
          // modelExprs.add(expr);
        }
        catch(RuntimeException e){
          logger.debug("Failed to convert to Z3: "+pred.uniCond.toString());
          notConverted.add(pred);
        }
      
      }

      if(z3Con.isEmpty()){
        continue;
      }

      Expr<BoolSort>[] arrCon=new Expr[z3Con.size()];
      arrCon=z3Con.toArray(arrCon);

      if(arrCon.length>1){
        BoolExpr and1 = ctx.mkAnd(arrCon);
        boolean b=and1.isAnd();
  
        z3Dis.add(and1);
      }
      else{
        z3Dis.add(arrCon[0]);
      }
    }

    if(z3Dis.isEmpty()){
      return;
    }

    Expr<BoolSort>[] arrDis=new Expr[z3Dis.size()];
    arrDis=z3Dis.toArray(arrDis);

    Expr<BoolSort> allCombined=null;

    if(arrDis.length>1){
      allCombined=ctx.mkOr(arrDis).simplify();
    }
    else{
      allCombined=arrDis[0];
    }

    Goal goal=ctx.mkGoal(false, false, false);
    goal.add(allCombined);

    Tactic t = ctx.andThen(ctx.mkTactic("simplify"), ctx.mkTactic("propagate-values"), ctx.mkTactic("propagate-ineqs"), 
      ctx.mkTactic("ctx-solver-simplify"),
      ctx.mkTactic("unit-subsume-simplify"), 
      ctx.mkTactic("ctx-simplify")
      );

    ApplyResult ar= t.apply(goal);
    Goal[] subGoals = ar.getSubgoals();

    assert subGoals.length==1;

    this.simplifiedGoals.addAll(Arrays.asList(subGoals[0].getFormulas()));
  }
}
