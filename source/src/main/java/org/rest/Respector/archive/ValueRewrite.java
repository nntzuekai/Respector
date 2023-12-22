package org.rest.Respector.archive;

import soot.Value;
import soot.jimple.Expr;
import soot.jimple.IdentityRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.Local;
import soot.jimple.Constant;
import soot.jimple.Ref;
import soot.ValueBox;
import soot.Body;
import soot.BriefUnitPrinter;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValueRewrite {
  public String str;
  public Value v;

  public ValueRewrite(String str, Value v){
    this.str=str;
    this.v=v;
  }

  public ValueRewrite(){

  }
  
  static String printBrief(Value v, Body b){
    BriefUnitPrinter pt=new BriefUnitPrinter(b);
    pt.noIndent();

    v.toString(pt);
    return pt.toString();
  }

  static String regexNameMatch(String name){
    return "(?<!\\w)"+ Pattern.quote(name)+"(?!\\w)";
  }

  static String replacementName(String name){
    return Matcher.quoteReplacement("("+name+")");
  }

  static String rewriteValueWith(Value v, HashMap<Local, ValueRewrite> flowSet, Body b){
    // return "";

    
    if(v instanceof Constant){
      return printBrief(v, b);
    }

    if(v instanceof Local){
      return flowSet.get((Local)v).str;
    }

    if(v instanceof Expr){
      if(v instanceof InvokeExpr){
        ///TODO: eliminate Double.valueOf and Integer.valueOf?

        if(v instanceof InstanceInvokeExpr){
          InstanceInvokeExpr expr=(InstanceInvokeExpr) v;

          String raw=printBrief(expr, b);
          for(Value uv:expr.getArgs()){
            if(uv instanceof Local){
              Local l=(Local) uv;
              String reg=regexNameMatch(l.getName());

              if(flowSet.containsKey(l)){
                String replStr=flowSet.get(l).str;
                raw=raw.replaceAll(reg, replacementName(replStr));
              }
              else{
                String replStr=l.getName();
                raw=raw.replaceAll(reg, String.format("<%s, not in flowSet>", replacementName(replStr)));
              } 
            }  
          }

          Value base=expr.getBase();
          if(base instanceof Local){
            Local l=(Local) base;
            String reg=regexNameMatch(l.getName());

            if(flowSet.containsKey(l)){
              String replStr=flowSet.get(l).str;
              raw=raw.replaceAll(reg, replacementName(replStr));
            }
          }
          return raw;
        }
        else{

          InvokeExpr expr=(InvokeExpr) v;

          String raw=printBrief(expr, b);
          for(Value uv:expr.getArgs()){
            if(uv instanceof Local){
              Local l=(Local) uv;
              String reg=regexNameMatch(l.getName());

              if(flowSet.containsKey(l)){
                String replStr=flowSet.get(l).str;
                raw=raw.replaceAll(reg, replacementName(replStr));
              }
              else{
                String replStr=l.getName();
                raw=raw.replaceAll(reg, String.format("<%s, not in flowSet>", replacementName(replStr)));
              } 
            }  
          }
          return raw;
        }

      }
      else{
        Expr expr=(Expr) v;

        String raw=printBrief(expr, b);
        
        List<ValueBox> ubs= expr.getUseBoxes();

        for(ValueBox vb:ubs){
          Value uv=vb.getValue();

          if(uv instanceof Local && flowSet.containsKey((Local)uv)){
            Local l=(Local) uv;
            String reg=regexNameMatch(l.getName());
            String replStr=flowSet.get(l).str;
            raw=raw.replaceAll(reg, replacementName(replStr));
          }
        }

        return raw;
      }
    }

    if(v instanceof Ref){
      if(v instanceof IdentityRef){
        return printBrief(v, b);
      }
      // ConcreteRef
      else{
        Ref ref=(Ref) v;

        String raw=printBrief(ref, b);
        
        List<ValueBox> ubs= ref.getUseBoxes();

        for(ValueBox vb:ubs){
          Value uv=vb.getValue();

          if(uv instanceof Local){
            Local l=(Local) uv;
            String reg=regexNameMatch(l.getName());
            
            
            if(flowSet.containsKey((Local)uv)){
              String replStr=flowSet.get(l).str;
              raw=raw.replaceAll(reg, replacementName(replStr));
            }
            else{
              String replStr=l.getName();
              raw=raw.replaceAll(reg, String.format("<%s, not in flowSet>", replacementName(replStr)));
            }
          }
        }
        return raw;
      }
    }

    throw new RuntimeException("Should not reach here!");
  }
}
