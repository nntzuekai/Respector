package org.rest.Respector.archive.GExprTranslate;

import java.util.List;

import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.EndPointParameter;

import soot.SootMethodRef;
import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.EqExpr;
import soot.jimple.IntConstant;
import soot.jimple.NeExpr;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;

public class SchemaPattern {
  public String pattern;
  public EndPointParameter epp;

  public SchemaPattern(String pattern, EndPointParameter epp) {
    this.pattern = pattern;
    this.epp = epp;
  }

  public static SchemaPattern toPattern(ConditionPred pred) {
    if(!(pred.evalTo instanceof Boolean && pred._pred instanceof ConditionExpr)){
      return null;
    }

    Value op1=pred.uniCond.getOp1();
    Value op2=pred.uniCond.getOp2();
    if(pred.uniCond instanceof EqExpr){

      if(op1 instanceof VirtualInvokeExpr && op2 instanceof IntConstant){
        VirtualInvokeExpr lhs=(VirtualInvokeExpr) op1;
        IntConstant rhs=(IntConstant) op2;

        Value base=lhs.getBase();
        if(rhs.value==1 && base instanceof EndPointParameter){
          EndPointParameter epp=(EndPointParameter)base;

          SootMethodRef ref= lhs.getMethodRef();
          if(ref.getDeclaringClass().getName().equals("java.lang.String") && ref.getName().equals("matches")){
            List<Value> args= lhs.getArgs();
            assert (args.size()==1);

            Value arg1=args.get(0);
            if(arg1 instanceof StringConstant){
              StringConstant strCon=(StringConstant) arg1;
              return new SchemaPattern(strCon.value, epp);
            }
          }
        }
      }
    }

    return null;
  }
}
