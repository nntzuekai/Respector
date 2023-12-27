# 1. A fatal error has been detected by the Java Runtime Environment

If you encountered a crash similar to the following:

```
A fatal error has been detected by the Java Runtime Envirnment:

SIGSEGV (0xb) pc=0x000075f1715.........

JRE version: ...............
Java VM: OpenJDK ........
Problematic frame:
C  [libc.so.6+0x1b1cc1]

.....
```

It is likely that you are using the pre-built Z3 that we provided, which was built on ***Ubuntu 20.04 on AMD64***. If you are on a different OS or platform, please following the instructions in `source/README.md` to build you own Z3.

# 2. NullPointerException in GDynamicInvokeExpr

If Respector crashed when running with

```
Exception in thread "main" java.lang.NullPointerException
        at soot.grimp.internal.GDynamicInvoleExpr.<init>(GDynamicInvoleExpr.java:57)
        ...
        ...
```

**You might have installed some version of Soot in your Maven local repository.**

To verify this, check if `~/.m2/repository/org/soot-oss/` exists on your machine. 

If so, ***remove this folder***, otherwise Maven is using the Soot there instead of the version we provided in `source/lib/local_repo/`. The original Soot contains a bug and we provided a version of Soot for which we fixed the bug. 

# 3. Differences between your generated specs and the ones we attached.

If you see your generated specification different from what we attacthed: 

Usually that is a **different ordering** of endpoint methods and global variables, which depends on how Soot loads the class files (which calls `java.io.File` to find all files under a directory). It is normal to have such differences if you have recompiled the target classes or you have a JVM of different version. 

When you see the numbers are different, you can check the total number of lines in the generated specification. If it is the same as the total number of lines in the specification we attached, then it is just the ordering issue.

In some cases, if you have recompiled the target service, you may notice the name of some lambda function (appearing in some constraints) is different. For example, in the generated specification you see `com.senzing.api.services.WhyServices$lambda_whyEntities_3__23` while in the specification we attached you see `com.senzing.api.services.WhyServices$lambda_whyEntities_3__62`. This is because you recompiled the service using a Java compiler of different version, which named those lambda functions differently.

# 4. The `operationId`s of endpoint methods are not in ascending order.

This is normal because we order the endpoint methods alphabetically based on their `path`s, while their `operationId`s are determined by the order Soot loaded classes.