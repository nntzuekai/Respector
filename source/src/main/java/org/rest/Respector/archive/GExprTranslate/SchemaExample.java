package org.rest.Respector.archive.GExprTranslate;

import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.EndPointParameter;

import soot.SootMethodRef;
import soot.Value;
import soot.jimple.ArithmeticConstant;
import soot.jimple.ConditionExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IntConstant;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NumericConstant;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;


public class SchemaExample {
  public Object example;
  public EndPointParameter epp;

  public SchemaExample(Object example, EndPointParameter epp) {
    this.example = example;
    this.epp = epp;
  }
  
  public static SchemaExample toExample(ConditionPred pred) {
    if(!(pred.evalTo instanceof Boolean && pred._pred instanceof ConditionExpr)){
      return null;
    }

    if(pred.uniCond instanceof EqExpr){
      EqExpr expr=(EqExpr) pred.uniCond;

      Value op1=expr.getOp1();
      Value op2=expr.getOp2();

      if(op1 instanceof EndPointParameter){
        EndPointParameter lhs=(EndPointParameter)op1;
        if(op2 instanceof IntConstant){
          IntConstant rhs=(IntConstant) op2;

          return new SchemaExample(rhs.value, lhs);
        }
        else if(op2 instanceof StringConstant){
          StringConstant rhs=(StringConstant) op2;

          return new SchemaExample(rhs.value, lhs);
        }
      }
      else if(op2 instanceof EndPointParameter){
        assert false: "EPP on RHS?";
      }
    }

    return null;
  }
}
