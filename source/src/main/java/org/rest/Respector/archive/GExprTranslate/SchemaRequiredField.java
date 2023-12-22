package org.rest.Respector.archive.GExprTranslate;

import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.EndPointParameter;

import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.EqExpr;
import soot.jimple.NeExpr;
import soot.jimple.NullConstant;

public class SchemaRequiredField {
  public boolean isRequired;
  public EndPointParameter epp;


  public SchemaRequiredField(boolean isRequired, EndPointParameter epp) {
    this.isRequired = isRequired;
    this.epp = epp;
  }


  public static SchemaRequiredField toRequiredField(ConditionPred pred) {
    if(!(pred.evalTo instanceof Boolean && pred._pred instanceof ConditionExpr)){
      return null;
    }

    Value op1=pred.uniCond.getOp1();
    Value op2=pred.uniCond.getOp2();

    if(pred.uniCond instanceof NeExpr){

      if(op1 instanceof EndPointParameter && op2 instanceof NullConstant){
        EndPointParameter epp=(EndPointParameter)op1;

        if(epp.isEPP){
          return new SchemaRequiredField(true, epp);
        }

      }
    }

    return null;
  }
}
