package org.rest.Respector.archive;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FrameworkData {
  public enum FrameworkName {
    Unknown,
    Spring,
    JAX,
  }

  public final FrameworkName name;
  public final String fullName;
  public final String[] packageNames;
  public final List<String> methodAnnotations;
  public final List<String> paramAnnotations;
  
  public FrameworkData(FrameworkName name, String fullName, String[] packageNames, String[] methodAnnotations, String[] paramAnnotations){
    this.name=name;
    this.fullName=fullName;
    this.packageNames=packageNames.clone();
    this.methodAnnotations=List.of(methodAnnotations);
    this.paramAnnotations=List.of(paramAnnotations);
  }
  
  public static final InputStream is= FrameworkData.class.getClassLoader().getResourceAsStream("FrameworkData.json");
  
  public static final FrameworkData[] Data={
    new FrameworkData(
      FrameworkName.Spring, 

      "Spring Framework",

      new String[]{
        "org.springframework",
      },

      new String[]{
        "Lorg/springframework/web/bind/annotation/GetMapping;",
        "Lorg/springframework/web/bind/annotation/RequestMapping;",
        "Lorg/springframework/web/bind/annotation/PostMapping;",
      },

      new String[]{
        "Lorg/springframework/web/bind/annotation/PathVariable;",
        "Lorg/springframework/web/bind/annotation/RequestParam;",
        // "Lorg/springframework/web/bind/annotation/RequestBody;",
        "Lorg/springframework/web/bind/annotation/RequestHeader;",
      }
    ),

    new FrameworkData(
      FrameworkName.JAX, 

      "JAX-RS",

      new String[]{
        "javax.ws.rs",
      }, 

      new String[]{
        "Ljavax/ws/rs/GET;",
      }, 
      
      new String[]{
        "Ljavax/ws/rs/QueryParam;",
        "Ljavax/ws/rs/PathParam;",
      }
    )
  };

  public static final EnumMap<FrameworkName, FrameworkData> DataMap=Stream.of(Data).collect(Collectors.toMap(
    d -> d.name, 
    Function.identity(),
    (l, r) -> {throw new IllegalArgumentException("Duplicate keys " + l + "and " + r + ".");},
    () -> new EnumMap<FrameworkName, FrameworkData>(FrameworkName.class)
    )
  );
}
