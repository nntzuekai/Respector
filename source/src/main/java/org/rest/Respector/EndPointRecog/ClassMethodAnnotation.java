package org.rest.Respector.EndPointRecog;

import java.util.List;
import java.util.TreeMap;
import java.util.ArrayList;

import soot.tagkit.AnnotationTag;
import soot.tagkit.AnnotationArrayElem;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationEnumElem;
import soot.tagkit.AnnotationStringElem;

import com.google.gson.annotations.SerializedName;

public class ClassMethodAnnotation {
  public String methodParam;
  public List<String> pathParam;
  public String produceParam;
  public String consumeParam;

  
  public List<String> statusCodeParam;

  @SerializedName("method")
  public String requestMethod;

  Integer getResponseStatus(AnnotationTag tag, TreeMap<String, StaticResponseInfo> nameToResponse){
    Integer status=null;

    if(statusCodeParam==null || statusCodeParam.isEmpty()){
      return status;
    }

    for(AnnotationElem elem0: tag.getElems()){
      if(elem0 instanceof AnnotationEnumElem){
        AnnotationEnumElem elem=(AnnotationEnumElem) elem0;

        String constantName=elem.getConstantName();

        if(nameToResponse.containsKey(constantName)){
          status=nameToResponse.get(constantName).statusCode;
          break;
        }
      }

    }

    return status;
  }

  ArrayList<String> getPathFrom(AnnotationTag tag){
    ArrayList<String> path=new ArrayList<>();

    if(pathParam ==null || pathParam.isEmpty()){
      return path;
    }

    for(AnnotationElem elem0: tag.getElems()){
      if(elem0 instanceof AnnotationStringElem){
        AnnotationStringElem elem=(AnnotationStringElem) elem0;

        if(pathParam.contains(elem.getName())){
          path.add(elem.getValue());
          break;
        }
      }
      else if(elem0 instanceof AnnotationArrayElem){
        AnnotationArrayElem elem = (AnnotationArrayElem) elem0;

        if(pathParam.contains(elem.getName())){
          for(AnnotationElem elem2: elem.getValues()){
            if(elem2 instanceof AnnotationStringElem){
              AnnotationStringElem elem3=(AnnotationStringElem) elem2;

              path.add(elem3.getValue());
            }
          }

          break;
        }
      }
    }

    return path;
  }

  ArrayList<String> getRequestMethodFrom(AnnotationTag tag){
    ArrayList<String> m=new ArrayList<>();

    if(requestMethod != null){
      m.add(requestMethod);
      return m;
    }

    if(methodParam == null){
      return m;
    }


    for(AnnotationElem elem0: tag.getElems()){
      if(elem0 instanceof AnnotationStringElem){
        AnnotationStringElem elem=(AnnotationStringElem) elem0;

        if(methodParam.equals(elem.getName())){
          m.add(elem.getValue().toLowerCase());
          break;
        }
      }
      else if(elem0 instanceof AnnotationArrayElem){
        AnnotationArrayElem elem = (AnnotationArrayElem) elem0;

        if(methodParam.contains(elem.getName())){
          for(AnnotationElem elem2: elem.getValues()){
            if(elem2 instanceof AnnotationStringElem){
              AnnotationStringElem elem3=(AnnotationStringElem) elem2;

              m.add(elem3.getValue().toLowerCase());
            }
            else if(elem2 instanceof AnnotationEnumElem){
              AnnotationEnumElem elem3=(AnnotationEnumElem) elem2;

              m.add(elem3.getConstantName().toLowerCase());
            }
          }

          break;
        }
      }
    }

    return m;
  }
}
