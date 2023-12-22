package com.urcodebin.api.enums;

public enum PasteSyntax {
    NONE ("None"),
    JAVA ("Java"),
    CSHARP ("C#"),
    Python ("Python"),
    JAVASCRIPT ("JavaScript"),
    GO ("Go"),
    CLANG ("C"),
    CPLUSPLUS ("C++"),
    PHP ("PHP"),
    SWIFT ("Swift"),
    LUA ("Lua"),
    RUBY ("Ruby"),
    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL");

    private final String value;
    PasteSyntax(String value) {
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }
}
