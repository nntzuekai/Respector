package org.rest.Respector.Z3ToOAS;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.function.Function;

import org.rest.Respector.PathCondExtract.EndPointParameter;
import org.rest.Respector.Simplification.SootToZ3;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.ParameterObj;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.StringSymbol;
import com.microsoft.z3.Symbol;

import soot.Value;
import soot.jimple.VirtualInvokeExpr;

public class ToParamKeyword {
  public static ArrayList<BoolExpr> translate(ArrayList<BoolExpr> disjunction, EndPointParameter epp, ParameterObj param, SootToZ3 toZ3) {
    
    int nDis=disjunction.size();

    ArrayList<BoolExpr> notTranslated=new ArrayList<>();
    for(int i=0;i<nDis;++i){
      BoolExpr expr=disjunction.get(i);

      boolean rtv;

      rtv=toStrLength(expr, epp, param, toZ3);
      if(rtv){
        continue;
      }

      rtv=toMinMax(expr, epp, param, toZ3);
      if(rtv){
        continue;
      }

      rtv=toMultipleOf(expr, epp, param, toZ3);
      if(rtv){
        continue;
      }

      rtv=toRequiredField(expr, epp, param, toZ3);
      if(rtv){
        continue;
      }

      notTranslated.add(expr);
    }

    return notTranslated;
  }

  public static boolean toRequiredField(BoolExpr expr, EndPointParameter epp, ParameterObj param, SootToZ3 toZ3) {
    if(expr.isNot()){
      Expr expr1=expr.getArgs()[0];
      if(expr1.isEq()){
        Expr[] args = expr1.getArgs();
      
        assert args.length==2;

        Expr lhs=args[0];
        Expr rhs=args[1];

        if(rhs.isConst()){
          Symbol sym = rhs.getFuncDecl().getName();
          if(sym.isStringSymbol()){
            if(((StringSymbol)sym).getString().equals("null")){
              param.setRequired(true);

              return true;
            }
          }
        }
        else if(lhs.isConst()){
          Symbol sym = lhs.getFuncDecl().getName();
          if(sym.isStringSymbol()){
            if(((StringSymbol)sym).getString().equals("null")){
              param.setRequired(true);

              return true;
            }
          }
        }
      }
    }

    return false;
  }

  public static boolean toMultipleOf(BoolExpr expr, EndPointParameter epp, ParameterObj param, SootToZ3 toZ3){
    if(expr.isEq()){
      Expr[] args = expr.getArgs();
      
      assert args.length==2;

      Expr lhs=args[0];
      Expr rhs=args[1];

      if(rhs.isIntNum() && lhs.isRemainder()){
        Expr[] lhsArgs=lhs.getArgs();

        assert args.length==2;

        Expr op1=lhsArgs[0];
        Expr op2=lhsArgs[1];


        if(op2.isIntNum() && toZ3.varMapRev.containsKey(op1)){
          Value v=toZ3.varMapRev.get(op1);

          assert v.equivTo(epp);
          if(v.equivTo(epp)){

            int m=((IntNum)op2).getInt();

            param.schema.addMultipleOf(m);
            
            return true;
          }
        }
      }
    }

    return false;
  }

  public static boolean toMinMax(BoolExpr expr, EndPointParameter epp, ParameterObj param, SootToZ3 toZ3){
    if(expr.isEq() || expr.isLE() || expr.isLT() || expr.isGE() || expr.isGT()){
      Expr[] args = expr.getArgs();
      
      assert args.length==2;

      Expr lhs=args[0];
      Expr rhs=args[1];

      if(rhs.isIntNum()){
        if(toZ3.varMapRev.containsKey(lhs)){
          Value v=toZ3.varMapRev.get(lhs);

          assert v.equivTo(epp);
          if(v.equivTo(epp)){

            int m=((IntNum)rhs).getInt();

            if(expr.isEq()){
              param.schema.addMaximum(m);
              param.schema.addExclusiveMaxium(false);
              param.schema.addMinimum(m);
              param.schema.addExclusiveMinimum(false);
            }
            else if(expr.isLT()){
              param.schema.addMaximum(m);
              param.schema.addExclusiveMaxium(true);
            }
            else if(expr.isLE()){
              param.schema.addMaximum(m);
              param.schema.addExclusiveMaxium(false);
            }
            else if(expr.isGT()){
              param.schema.addMinimum(m);
              param.schema.addExclusiveMinimum(true);
            }
            // isGE
            else{
              param.schema.addMinimum(m);
              param.schema.addExclusiveMinimum(false);
            }
            
            return true;
          }
        }
      }
    }

    return false;
  }

  public static boolean toStrLength(BoolExpr expr, EndPointParameter epp, ParameterObj param, SootToZ3 toZ3) {
    
    if(expr.isEq() || expr.isLE() || expr.isLT() || expr.isGE() || expr.isGT()){
      Expr[] args = expr.getArgs();
      
      assert args.length==2;

      Expr lhs=args[0];
      Expr rhs=args[1];

      if(rhs.isIntNum()){
        if(toZ3.varMapRev.containsKey(lhs)){
          Value v=toZ3.varMapRev.get(lhs);
          if(v instanceof VirtualInvokeExpr){
            VirtualInvokeExpr callLenOf=(VirtualInvokeExpr) v;

            assert callLenOf.getBase().equivTo(epp);

            if(callLenOf.getMethodRef().getName().equals("length")){

              int m=((IntNum)rhs).getInt();
              
              if(expr.isEq()){
                param.schema.addMaxLength(m);
                param.schema.addMinLength(m);
              }
              else if(expr.isLT()){
                param.schema.addMaxLength(m+1);
              }
              else if(expr.isLE()){
                param.schema.addMaxLength(m);
              }
              else if(expr.isGT()){
                param.schema.addMinLength(m+1);
              }
              // isGE
              else{
                param.schema.addMinLength(m);
              }
              
              return true;
            }
          }
        }
      }
    }
    
    
    return false;
    
  }

  @Deprecated
  public static boolean maximumLength(BoolExpr expr, EndPointParameter epp, ParameterObj param, SootToZ3 toZ3) {
    
    if(expr.isLE() || expr.isLT()){
      Expr[] args = expr.getArgs();
      
      assert args.length==2;

      Expr lhs=args[0];
      Expr rhs=args[1];

      if(rhs.isIntNum()){
        if(toZ3.varMapRev.containsKey(lhs)){
          Value v=toZ3.varMapRev.get(lhs);
          if(v instanceof VirtualInvokeExpr){
            VirtualInvokeExpr callLenOf=(VirtualInvokeExpr) v;

            assert callLenOf.getBase().equivTo(epp);

            if(callLenOf.getMethodRef().getName().equals("length")){

              int m=((IntNum)rhs).getInt();
              if(expr.isLT()){
                param.schema.addMaxLength(m+1);
              }
              else{
                param.schema.addMaxLength(m);
              }

              return true;
            }
          }
        }
      }
    }
    
    
    return false;
    
  }

  @Deprecated
  public static boolean minimumLength(BoolExpr expr, EndPointParameter epp, ParameterObj param, SootToZ3 toZ3) {
    
    if(expr.isGE() || expr.isGT()){
      Expr[] args = expr.getArgs();
      
      assert args.length==2;

      Expr lhs=args[0];
      Expr rhs=args[1];

      if(rhs.isIntNum()){
        if(toZ3.varMapRev.containsKey(lhs)){
          Value v=toZ3.varMapRev.get(lhs);
          if(v instanceof VirtualInvokeExpr){
            VirtualInvokeExpr callLenOf=(VirtualInvokeExpr) v;

            assert callLenOf.getBase().equivTo(epp);

            if(callLenOf.getMethodRef().getName().equals("length")){

              int m=((IntNum)rhs).getInt();
              if(expr.isGT()){
                param.schema.addMinLength(m+1);
              }
              else{
                param.schema.addMinLength(m);
              }

              return true;
            }
          }
        }
      }
    }
    
    
    return false;
    
  }
}
