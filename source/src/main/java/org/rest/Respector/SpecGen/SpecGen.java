package org.rest.Respector.SpecGen;

import org.rest.Respector.SpecGen.MediaType.Schema.MediaTypeSchemaObj;
import org.rest.Respector.SpecGen.MediaType.Schema.MediaTypeSchemaObjSerializer;
import org.rest.Respector.SpecGen.Spec.Components.GlobalVarInfo;
import org.rest.Respector.SpecGen.Spec.Components.GlobalVarInfoSerializer;
import org.rest.Respector.SpecGen.Spec.Components.SpecComponentsObj;
import org.rest.Respector.SpecGen.Spec.Components.SpecComponentsObjSerializer;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XPathItemObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XPathItemObjSerializer;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.Constraints.ConstraintsObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.Constraints.ConstraintsObjSerializer;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalRefs.GlobalRefObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalRefs.GlobalRefObjSerializer;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalWrites.GlobalWriteObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalWrites.GlobalWriteSerializer;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointConstraints.XEndPointConstraintsObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointConstraints.XEndPointConstraintsObjSerializer;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointMethodConstraint.XEndPointMethodConstraintObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointMethodConstraint.XEndPointMethodConstraintObjSerializer;
import org.rest.Respector.SpecGen.Spec.Components.InterDependence.GlobalInterDepObj;
import org.rest.Respector.SpecGen.Spec.Components.InterDependence.GlobalInterDepSerializer;
import org.rest.Respector.SpecGen.Spec.Path.PathItemObj;
import org.rest.Respector.SpecGen.Spec.Path.PathItemObjSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SpecGen {
  public static Gson getSpecBuilder() {
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
    .registerTypeAdapter(PathItemObj.class, new PathItemObjSerializer())
    .registerTypeAdapter(XPathItemObj.class, new XPathItemObjSerializer())
    .registerTypeAdapter(GlobalVarInfo.class, new GlobalVarInfoSerializer())
    .registerTypeHierarchyAdapter(GlobalRefObj.class, new GlobalRefObjSerializer())
    .registerTypeHierarchyAdapter(GlobalWriteObj.class, new GlobalWriteSerializer())
    .registerTypeAdapter(ConstraintsObj.class, new ConstraintsObjSerializer())
    .registerTypeAdapter(XEndPointConstraintsObj.class, new XEndPointConstraintsObjSerializer())
    .registerTypeAdapter(GlobalInterDepObj.class, new GlobalInterDepSerializer())
    .registerTypeAdapter(MediaTypeSchemaObj.class, new MediaTypeSchemaObjSerializer())
    .registerTypeAdapter(SpecComponentsObj.class, new SpecComponentsObjSerializer())
    .registerTypeAdapter(XEndPointMethodConstraintObj.class, new XEndPointMethodConstraintObjSerializer())
    .create();

    return gson;
  }
}
