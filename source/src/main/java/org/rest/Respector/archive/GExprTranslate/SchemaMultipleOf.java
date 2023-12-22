package org.rest.Respector.archive.GExprTranslate;

import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.EndPointParameter;

import soot.SootMethodRef;
import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.DivExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IntConstant;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.VirtualInvokeExpr;


public class SchemaMultipleOf {
  public int multipleOf;
  public EndPointParameter epp;

  public SchemaMultipleOf(int multipleOf, EndPointParameter epp) {
    this.multipleOf = multipleOf;
    this.epp = epp;
  }

  public static SchemaMultipleOf toMultipleOf(ConditionPred pred) {
    if(!(pred.evalTo instanceof Boolean && pred._pred instanceof ConditionExpr)){
      return null;
    }

    Value op1=pred.uniCond.getOp1();
    Value op2=pred.uniCond.getOp2();
    if(pred.uniCond instanceof EqExpr){
      if(op1 instanceof EndPointParameter){
        EndPointParameter epp=(EndPointParameter) op1;

        if(op2 instanceof MulExpr){
          MulExpr rhs=(MulExpr) op2;

          if(rhs.getOp1() instanceof IntConstant){
            IntConstant m=(IntConstant) rhs.getOp1();
  
            return new SchemaMultipleOf(m.value, epp);
          }
          else if(rhs.getOp2() instanceof IntConstant){
            IntConstant m=(IntConstant) rhs.getOp2();
  
            return new SchemaMultipleOf(m.value, epp);
          }

        }
      }
      else if(op1 instanceof DivExpr && op2 instanceof IntConstant){
        DivExpr lhs=(DivExpr) op1;
        IntConstant rhs=(IntConstant) op2;

        if(rhs.value==0){
          if(lhs.getOp1() instanceof EndPointParameter){
            EndPointParameter epp=(EndPointParameter)(lhs.getOp1());

            if(lhs.getOp2() instanceof IntConstant){
              IntConstant mul=(IntConstant) lhs.getOp2();
              return new SchemaMultipleOf(mul.value, epp);    
            }
          }
        }
      
      }
      
    }

    // boolean evalTo=(Boolean)pred.evalTo;
    // if(evalTo==true){
    //   if(pred.pred instanceof EqExpr){
    //     EqExpr expr=(EqExpr) pred.pred;

    //     if(expr.getOp2() instanceof MulExpr && expr.getOp1() instanceof EndPointParameter){
    //       EndPointParameter op1=(EndPointParameter) expr.getOp1();
    //       MulExpr op2=(MulExpr) expr.getOp2();

    //       if(op2.getOp1() instanceof IntConstant){
    //         IntConstant m=(IntConstant) op2.getOp1();

    //         return new SchemaMultipleOf(m.value, op1);
    //       }
    //       else if(op2.getOp2() instanceof IntConstant){
    //         IntConstant m=(IntConstant) op2.getOp2();

    //         return new SchemaMultipleOf(m.value, op1);
    //       }
    //     }
    //     else if(expr.getOp1() instanceof DivExpr && expr.getOp2() instanceof IntConstant){
    //       DivExpr op1=(DivExpr) expr.getOp1();
    //       IntConstant op2=(IntConstant) expr.getOp2();

    //       if(op2.value==0){
    //         if(op1.getOp1() instanceof EndPointParameter && op1.getOp2() instanceof IntConstant){
    //           IntConstant mul=(IntConstant) op1.getOp2();
    //           return new SchemaMultipleOf(mul.value, (EndPointParameter)(op1.getOp1()));
    //         }
    //       }
        
    //     }
    //   }
    // }
    // else{
    //   if(pred.pred instanceof NeExpr){
    //     NeExpr expr=(NeExpr) pred.pred;

    //     if(expr.getOp2() instanceof MulExpr && expr.getOp1() instanceof EndPointParameter){
    //       EndPointParameter op1=(EndPointParameter) expr.getOp1();
    //       MulExpr op2=(MulExpr) expr.getOp2();

    //       if(op2.getOp1() instanceof IntConstant){
    //         IntConstant m=(IntConstant) op2.getOp1();

    //         return new SchemaMultipleOf(m.value, op1);
    //       }
    //       else if(op2.getOp2() instanceof IntConstant){
    //         IntConstant m=(IntConstant) op2.getOp2();

    //         return new SchemaMultipleOf(m.value, op1);
    //       }
    //     }
    //     else if(expr.getOp1() instanceof DivExpr && expr.getOp2() instanceof IntConstant){
    //       DivExpr op1=(DivExpr) expr.getOp1();
    //       IntConstant op2=(IntConstant) expr.getOp2();

    //       if(op2.value==0){
    //         if(op1.getOp1() instanceof EndPointParameter && op1.getOp2() instanceof IntConstant){
    //           IntConstant mul=(IntConstant) op1.getOp2();
    //           return new SchemaMultipleOf(mul.value, (EndPointParameter)(op1.getOp1()));
    //         }
    //       }
        
    //     }
    //   }
    // }
    
    
    return null;
  }

}
