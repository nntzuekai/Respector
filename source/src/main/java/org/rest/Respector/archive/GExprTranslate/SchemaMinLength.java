package org.rest.Respector.archive.GExprTranslate;

import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.EndPointParameter;

import soot.SootMethodRef;
import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IntConstant;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.VirtualInvokeExpr;

public class SchemaMinLength {
  public int minLength;
  public EndPointParameter epp;

  public SchemaMinLength(int minLength, EndPointParameter epp) {
    this.minLength = minLength;
    this.epp = epp;
  }


  public static SchemaMinLength toMinLength(ConditionPred pred) {
    if(!(pred.evalTo instanceof Boolean && pred._pred instanceof ConditionExpr)){
      return null;
    }

    Value op1=pred.uniCond.getOp1();
    Value op2=pred.uniCond.getOp2();
    if(pred.uniCond instanceof GeExpr){

      if(op1 instanceof VirtualInvokeExpr && op2 instanceof IntConstant){
        VirtualInvokeExpr lhs=(VirtualInvokeExpr)op1;
        IntConstant rhs=(IntConstant) op2;
        
        if(lhs.getBase() instanceof EndPointParameter){
          EndPointParameter epp=(EndPointParameter) lhs.getBase();

          SootMethodRef ref= lhs.getMethodRef();
          if(ref.getDeclaringClass().getName().equals("java.lang.String") && ref.getName().equals("length")){
            return new SchemaMinLength(rhs.value, epp);
          }
        }
      }
    }
    else if(pred.uniCond instanceof GtExpr){

      if(op1 instanceof VirtualInvokeExpr && op2 instanceof IntConstant){
        VirtualInvokeExpr lhs=(VirtualInvokeExpr)op1;
        IntConstant rhs=(IntConstant) op2;
        
        if(lhs.getBase() instanceof EndPointParameter){
          EndPointParameter epp=(EndPointParameter) lhs.getBase();

          SootMethodRef ref= lhs.getMethodRef();
          if(ref.getDeclaringClass().getName().equals("java.lang.String") && ref.getName().equals("length")){
            return new SchemaMinLength(rhs.value+1, epp);
          }
        }
      }
    }
    else if(pred.uniCond instanceof EqExpr){

      if(op1 instanceof VirtualInvokeExpr && op2 instanceof IntConstant){
        VirtualInvokeExpr lhs=(VirtualInvokeExpr)op1;
        IntConstant rhs=(IntConstant) op2;

        if(lhs.getBase() instanceof EndPointParameter){
          EndPointParameter epp=(EndPointParameter) lhs.getBase();

          SootMethodRef ref= lhs.getMethodRef();
          if(ref.getDeclaringClass().getName().equals("java.lang.String") && ref.getName().equals("length")){
            return new SchemaMinLength(rhs.value, epp);
          }
        }
      }
    }

    // boolean evalTo=(Boolean)pred.evalTo;

    // if(evalTo==true){
    //   if(pred.pred instanceof GeExpr){
    //     GeExpr expr=(GeExpr) pred.pred;

    //     if(expr.getOp2() instanceof IntConstant && expr.getOp1() instanceof VirtualInvokeExpr){
    //       IntConstant op2=(IntConstant) expr.getOp2();
    //       VirtualInvokeExpr op1=(VirtualInvokeExpr) expr.getOp1();

    //       if(op1.getBase() instanceof EndPointParameter){
    //         EndPointParameter epp=(EndPointParameter) op1.getBase();

    //         SootMethodRef ref= op1.getMethodRef();
    //         if(ref.getDeclaringClass().getName().equals("java.lang.String") && ref.getName().equals("length")){
    //           return new SchemaMinLength(op2.value, epp);
    //         }
    //       }
    //     }
    //   }
    //   else if(pred.pred instanceof GtExpr){
    //     GtExpr expr=(GtExpr) pred.pred;

    //     if(expr.getOp2() instanceof IntConstant && expr.getOp1() instanceof VirtualInvokeExpr){
    //       IntConstant op2=(IntConstant) expr.getOp2();
    //       VirtualInvokeExpr op1=(VirtualInvokeExpr) expr.getOp1();

    //       if(op1.getBase() instanceof EndPointParameter){
    //         EndPointParameter epp=(EndPointParameter) op1.getBase();

    //         SootMethodRef ref= op1.getMethodRef();
    //         if(ref.getDeclaringClass().getName().equals("java.lang.String") && ref.getName().equals("length")){
    //           return new SchemaMinLength(op2.value+1, epp);
    //         }
    //       }
    //     }
    //   }
    //   else if(pred.pred instanceof EqExpr){
    //     EqExpr expr=(EqExpr) pred.pred;

    //     if(expr.getOp2() instanceof IntConstant && expr.getOp1() instanceof VirtualInvokeExpr){
    //       IntConstant op2=(IntConstant) expr.getOp2();
    //       VirtualInvokeExpr op1=(VirtualInvokeExpr) expr.getOp1();

    //       if(op1.getBase() instanceof EndPointParameter){
    //         EndPointParameter epp=(EndPointParameter) op1.getBase();

    //         SootMethodRef ref= op1.getMethodRef();
    //         if(ref.getDeclaringClass().getName().equals("java.lang.String") && ref.getName().equals("length")){
    //           return new SchemaMinLength(op2.value, epp);
    //         }
    //       }
    //     }
    //   }

    // }
    // else{
    //   if(pred.pred instanceof LtExpr){
    //     LtExpr expr=(LtExpr) pred.pred;

    //     if(expr.getOp2() instanceof IntConstant && expr.getOp1() instanceof VirtualInvokeExpr){
    //       IntConstant op2=(IntConstant) expr.getOp2();
    //       VirtualInvokeExpr op1=(VirtualInvokeExpr) expr.getOp1();

    //       if(op1.getBase() instanceof EndPointParameter){
    //         EndPointParameter epp=(EndPointParameter) op1.getBase();

    //         SootMethodRef ref= op1.getMethodRef();
    //         if(ref.getDeclaringClass().getName().equals("java.lang.String") && ref.getName().equals("length")){
    //           return new SchemaMinLength(op2.value, epp);
    //         }
    //       }
    //     }
    //   }
    //   else if(pred.pred instanceof LeExpr){
    //     LeExpr expr=(LeExpr) pred.pred;

    //     if(expr.getOp2() instanceof IntConstant && expr.getOp1() instanceof VirtualInvokeExpr){
    //       IntConstant op2=(IntConstant) expr.getOp2();
    //       VirtualInvokeExpr op1=(VirtualInvokeExpr) expr.getOp1();

    //       if(op1.getBase() instanceof EndPointParameter){
    //         EndPointParameter epp=(EndPointParameter) op1.getBase();

    //         SootMethodRef ref= op1.getMethodRef();
    //         if(ref.getDeclaringClass().getName().equals("java.lang.String") && ref.getName().equals("length")){
    //           return new SchemaMinLength(op2.value+1, epp);
    //         }
    //       }
    //     }
    //   }
    // }

    return null;
  }
}
