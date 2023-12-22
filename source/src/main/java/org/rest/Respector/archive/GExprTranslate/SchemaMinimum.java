package org.rest.Respector.archive.GExprTranslate;

import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.EndPointParameter;

import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IntConstant;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;

public class SchemaMinimum {
  public int minimum;
  public boolean exclusive;
  public EndPointParameter epp;

  public SchemaMinimum(int minimum, boolean exclusive, EndPointParameter epp) {
    this.minimum = minimum;
    this.exclusive = exclusive;
    this.epp = epp;
  }

  public static SchemaMinimum toMinimum(ConditionPred pred) {
    if(!(pred.evalTo instanceof Boolean && pred._pred instanceof ConditionExpr)){
      return null;
    }

    Value op1=pred.uniCond.getOp1();
    Value op2=pred.uniCond.getOp2();
    if(pred.uniCond instanceof GtExpr){

      if(op1 instanceof EndPointParameter && op2 instanceof IntConstant){
        EndPointParameter lhs=(EndPointParameter)op1;
        IntConstant rhs=(IntConstant) op2;
        
        return new SchemaMinimum(rhs.value, true, lhs);
      }
    }
    else if(pred.uniCond instanceof GeExpr){

      if(op1 instanceof EndPointParameter && op2 instanceof IntConstant){
        EndPointParameter lhs=(EndPointParameter)op1;
        IntConstant rhs=(IntConstant) op2;
        
        return new SchemaMinimum(rhs.value, false, lhs);
      }
    }

    // boolean evalTo=(Boolean)pred.evalTo;

    // if(evalTo==true){
    //   if(pred.pred instanceof GtExpr){
    //     GtExpr expr=(GtExpr) pred.pred;

    //     if(expr.getOp2() instanceof IntConstant && expr.getOp1() instanceof EndPointParameter){
    //       IntConstant op2=(IntConstant) expr.getOp2();
    //       EndPointParameter op1=(EndPointParameter) expr.getOp1();
          
    //       return new SchemaMinimum(op2.value, true, op1);
    //     }
    //   }
    //   else if(pred.pred instanceof GeExpr){
    //     GeExpr expr=(GeExpr) pred.pred;

    //     if(expr.getOp2() instanceof IntConstant && expr.getOp1() instanceof EndPointParameter){
    //       IntConstant op2=(IntConstant) expr.getOp2();
    //       EndPointParameter op1=(EndPointParameter) expr.getOp1();
          
    //       return new SchemaMinimum(op2.value, false, op1);
    //     }
    //   }
    // }
    // else{
    //   if(pred.pred instanceof LtExpr){
    //     LtExpr expr=(LtExpr) pred.pred;

    //     if(expr.getOp2() instanceof IntConstant && expr.getOp1() instanceof EndPointParameter){
    //       IntConstant op2=(IntConstant) expr.getOp2();
    //       EndPointParameter op1=(EndPointParameter) expr.getOp1();
          
    //       return new SchemaMinimum(op2.value, false, op1);
    //     }
    //   }
    //   else if(pred.pred instanceof LeExpr){
    //     LeExpr expr=(LeExpr) pred.pred;

    //     if(expr.getOp2() instanceof IntConstant && expr.getOp1() instanceof EndPointParameter){
    //       IntConstant op2=(IntConstant) expr.getOp2();
    //       EndPointParameter op1=(EndPointParameter) expr.getOp1();
          
    //       return new SchemaMinimum(op2.value, true, op1);
    //     }
    //   }
    // }


    return null;
  }

}
