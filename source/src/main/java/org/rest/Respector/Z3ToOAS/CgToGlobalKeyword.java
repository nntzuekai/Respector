package org.rest.Respector.Z3ToOAS;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.function.Function;

import org.rest.Respector.Simplification.SootToZ3;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalRefs.GlobalRefObj;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.StringSymbol;
import com.microsoft.z3.Symbol;

import soot.SootField;



public class CgToGlobalKeyword {
  public static ArrayList<BoolExpr> translate(ArrayList<BoolExpr> disjunction, SootField g, GlobalRefObj ref, SootToZ3 toZ3) {
    
    int nDis=disjunction.size();

    ArrayList<BoolExpr> notTranslated=new ArrayList<>();
    for(int i=0;i<nDis;++i){
      BoolExpr expr=disjunction.get(i);

      boolean rtv;

      rtv=toExamples(expr, g, ref, toZ3);
      if(rtv){
        continue;
      }

      notTranslated.add(expr);
    }

    return notTranslated;
  }

  public static boolean toExamples(BoolExpr expr, SootField g, GlobalRefObj ref, SootToZ3 toZ3) {
    

    return false;
  }
}
