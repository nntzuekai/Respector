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

public class SchemaMaximum {
  public int maximum;
  public boolean exclusive;
  public EndPointParameter epp;

  public SchemaMaximum(int maximum, boolean exclusive, EndPointParameter epp) {
    this.maximum = maximum;
    this.exclusive = exclusive;
    this.epp = epp;
  }

  public static SchemaMaximum toMaxium(ConditionPred pred) {
    if(!(pred.evalTo instanceof Boolean && pred._pred instanceof ConditionExpr)){
      return null;
    }

    Value op1=pred.uniCond.getOp1();
    Value op2=pred.uniCond.getOp2();
    if(pred.uniCond instanceof LtExpr){

      if(op1 instanceof EndPointParameter && op2 instanceof IntConstant){
        EndPointParameter lhs=(EndPointParameter)op1;
        IntConstant rhs=(IntConstant) op2;
        
        return new SchemaMaximum(rhs.value, true, lhs);
      }
    }
    else if(pred.uniCond instanceof LeExpr){

      if(op1 instanceof EndPointParameter && op2 instanceof IntConstant){
        EndPointParameter lhs=(EndPointParameter)op1;
        IntConstant rhs=(IntConstant) op2;
        
        return new SchemaMaximum(rhs.value, false, lhs);
      }
    }

    return null;
  }

}
