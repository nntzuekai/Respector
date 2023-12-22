package org.rest.Respector.EndPointRecog;

import java.util.List;

import soot.tagkit.AnnotationTag;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationIntElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationBooleanElem;

public class ParameterAnnotation {
  public enum paramLoction{
    query,
    path,
    header,
    cookie,
    formData,
    body
  }

  public List<String> nameParam;
  public String requiredParam;
  public paramLoction in;
  public String defaultValueParam;

  String getNameFrom(AnnotationTag tag){
    if(nameParam == null || nameParam.isEmpty()){
      return null;
    }

    String name=null;

    for(AnnotationElem elem0: tag.getElems()){
      if(elem0 instanceof AnnotationStringElem){
        AnnotationStringElem elem=(AnnotationStringElem) elem0;

        if(nameParam.contains(elem.getName())){
          name=elem.getValue();
          break;
        }
      }
    }

    return name;
  }

  Boolean getRequiredFrom(AnnotationTag tag){
    if(requiredParam == null){
      return null;
    }

    Boolean required=null;

    for(AnnotationElem elem0: tag.getElems()){
      if(elem0 instanceof AnnotationBooleanElem){
        AnnotationBooleanElem elem=(AnnotationBooleanElem) elem0;

        if(elem.getName().equals(requiredParam)){
          required=elem.getValue();
          break;
        }
      }
      else if(elem0 instanceof AnnotationIntElem){
        AnnotationIntElem elem=(AnnotationIntElem) elem0;

        if(elem.getName().equals(requiredParam)){
          required=elem.getValue()==1;
          break;
        }
      }
    }

    return required;
  }

  paramLoction getInFrom(AnnotationTag tag){
    return in;
  }

  String getDefaultValueFrom(AnnotationTag tag){
    if(defaultValueParam == null){
      return null;
    }

    String defaultValue=null;

    for(AnnotationElem elem0: tag.getElems()){
      if(elem0 instanceof AnnotationStringElem){
        AnnotationStringElem elem=(AnnotationStringElem) elem0;

        if(elem.getName().equals(defaultValueParam)){
          defaultValue=elem.getValue();
          break;
        }
      }
    }

    return defaultValue;
  }

}
