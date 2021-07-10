package com.bitmechanic.barrister;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Code generator.  Provides a command line interface for generating Java classes
 * based on an IDL JSON file.
 *
 * Usage:
 *
 * java com.bitmechanic.barrister.Idl2Java -j [idl file] -p [Java package name] -o [out dir]
 */
public class Idl2Java {

    /**
     * Runs the code generator on the command line.
     */
    public static void main(String argv[]) throws Exception {
        String idlFile = null;
        String pkgName = null;
        String outDir = null;
        String nsPkgName = null;
        boolean allImmutable = false;
        List<String> immutableSubstr = new ArrayList<String>();

        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-j")) {
                idlFile = argv[++i];
            }
            else if (argv[i].equals("-p")) {
                pkgName = argv[++i];
            }
            else if (argv[i].equals("-o")) {
                outDir = argv[++i];
            }
            else if (argv[i].equals("-b")) {
                nsPkgName = argv[++i];
            }
            else if (argv[i].equals("-s")) {
                immutableSubstr.add(argv[++i]);
            }
            else if (argv[i].equals("-i")) {
                allImmutable = true;
            }
        }

        if (isBlank(idlFile) || isBlank(pkgName) || isBlank(outDir)) {
            out("Usage: java com.bitmechanic.barrister.Idl2Java -j [idl file] -p [Java package prefix] -b [Java package prefix for namespaced entities] -o [out dir] -i -s [immutable class substr]");
            System.exit(1);
        }

        if (nsPkgName == null) {
            nsPkgName = pkgName;
        }

        new Idl2Java(idlFile, pkgName, nsPkgName, outDir, allImmutable, immutableSubstr);
    }

    private static boolean isBlank(String s) {
        return (s == null) || (s.trim().equals(""));
    }

    private static void out(String msg) {
        System.out.println(msg);
    }

    ///////////////////////////

    static String newline = System.getProperty("line.separator");

    static Map<String,String> typeConverters;
    static {
        typeConverters = new HashMap<String,String>();
        typeConverters.put("bool",   "com.bitmechanic.barrister.BoolTypeConverter");
        typeConverters.put("int",    "com.bitmechanic.barrister.IntTypeConverter");
        typeConverters.put("float",  "com.bitmechanic.barrister.FloatTypeConverter");
        typeConverters.put("string", "com.bitmechanic.barrister.StringTypeConverter");
    }

    private String outDir;
    private String pkgName;
    private String nsPkgName;
    private boolean allImmutable;
    private List<String> immutableSubstr;

    private Contract contract;
    private StringBuilder sb;
    
    public Idl2Java(String idlJson, String pkgName, String nsPkgName, String outDir, boolean allImmutable, List<String> immutableSubstr) throws Exception {
        out("Reading IDL from: " + idlJson);
        contract = Contract.load(new File(idlJson));

        out("Using package name: " + pkgName);
        this.pkgName = pkgName;

        this.allImmutable    = allImmutable;
        this.immutableSubstr = (immutableSubstr == null) ? new ArrayList<String>() : immutableSubstr;

        if (allImmutable) {
            out("Creating immutable struct classes");
        }
        else if (immutableSubstr != null && !immutableSubstr.isEmpty()) {
            out("Creating immutable struct classes for: " + immutableSubstr);
        }

        if (nsPkgName != null) {
            out("Using base package for namespaced entities: " + nsPkgName);
            this.nsPkgName = nsPkgName;
        } 

        this.outDir = outDir;

        for (Struct s : contract.getStructs().values()) {
            generate(s);
        }

        for (Enum e : contract.getEnums().values()) {
            generate(e);
        }

        for (Interface i : contract.getInterfaces().values()) {
            generate(i);
        }

        generate(contract.getMeta());
    }

    private void generate(Map<String,Object> meta) throws Exception {
        start(pkgName);
        line(0, "public class BarristerMeta {");
        line(0, "");
        for (Map.Entry<String,Object> entry : meta.entrySet()) {
            Object val = entry.getValue();
            String type = val.getClass().getSimpleName();
            if (val.getClass() == Long.class) {
              val = val + "L";
            }
            else if (val.getClass() == String.class) {
                val = "\"" + val + "\"";
            }
            line(1, "public static final " + type + " " + entry.getKey().toUpperCase() + " = " + val + ";");
        }
        line(1, "public static final String PACKAGE_NAME=\"" + pkgName + "\";");
        line(1, "public static final String NS_PACKAGE_NAME=\"" + nsPkgName + "\";");
        line(0, "");
        line(0, "}");
        toFile(pkgName, "BarristerMeta");
    }

    private void generate(Struct s) throws Exception {
        String pkgName = packageFor(s.getName());
        boolean immutable = isImmutable(pkgName+"."+s.getSimpleName());
        start(pkgName);
        boolean hasParent = false;
        String extend = " implements com.bitmechanic.barrister.BStruct";

        Map<String,Field> allFields = s.getFieldsPlusParents();
        Map<String,Field> myFields  = s.getFields();
        List<String> mapConstructorArgs  = new ArrayList<String>();

        StringBuilder allParams    = new StringBuilder();
        StringBuilder parentParams = new StringBuilder();

        for (String fieldName : s.getFieldNamesPlusParents()) {
            Field f = allFields.get(fieldName);
            if (!myFields.containsKey(fieldName)) {
                if (parentParams.length() > 0) parentParams.append(", ");
                parentParams.append(f.getName());
            }

            if (allParams.length() > 0) allParams.append(", ");
            allParams.append(newline).append("            @com.fasterxml.jackson.annotation.JsonProperty(\"").append(f.getName()).append("\") ")
                    .append(namespace(f.getJavaType())).append(" ").append(f.getName());

            mapConstructorArgs.add(String.format("            (%s)%s", namespace(f.getJavaType()), unmarshalFunc(f, "_map.get(\"" + f.getName() + "\")")));
        }

        if (!isBlank(s.getExtends())) {
            hasParent = true;
            extend = " extends " + namespace(s.getExtends());
        }
        line(0, "public class " + s.getSimpleName() + extend + " {");
        line(0, "");

        // Generate Builder inner class
        List<String> builderFields = new ArrayList<String>();
        line(0, "");
        line(1, "public static class Builder {");
        for (String name : s.getFieldNamesPlusParents()) {
            Field f = allFields.get(name);
            line(2, "private " + namespace(f.getJavaType()) + " _" + f.getName() + ";");
            line(2, "public Builder " + f.getName() + "(" + namespace(f.getJavaType()) +
                    " " + f.getName() + ") { " +
                    "this._" + f.getName() + " = " + f.getName() + "; return this; }");
            builderFields.add("this._" + f.getName());
        }
        line(2, "public " + s.getSimpleName() + " build() {");
        line(3, "return new " + s.getSimpleName() + "(" + join(builderFields, ", ") + ");");
        line(2, "}");

        line(0, "");
        line(2, "public Builder() { }");
        line(2, "public Builder(" + s.getSimpleName() + " obj) {");
        for (String name : s.getFieldNamesPlusParents()) {
            Field f = allFields.get(name);
            line(3, "this._"+f.getName()+" = obj.get"+f.getUpperName()+"();");
        }
        line(2, "}");
        line(1, "}");

        line(0, "");
        for (Field f : s.getFields().values()) {
            line(1, "private " + namespace(f.getJavaType()) + " " + f.getName() + ";");
        }

        if (!immutable) {
            line(0, "");
            line(1, "public " + s.getSimpleName() + "() {");
            line(2, "super();");
            line(1, "}");
        }

        // Generate constructors

        line(0, "");
        line(1, String.format("public %s(java.util.Map _map) throws com.bitmechanic.barrister.RpcException {", s.getSimpleName()));
        line(2, "this(");
        line(0, join(mapConstructorArgs, ","+newline));
        line(2, ");");
        line(1, "}");

        line(0, "");
        line(1, "@com.fasterxml.jackson.annotation.JsonCreator");
        line(1, "public " + s.getSimpleName() + "(" + allParams + ") {");
        line(2, "super(" + parentParams + ");");
        for (String fieldName : s.getFieldNames()) {
            Field f = s.getFields().get(fieldName);
            if (immutable && f.isArray()) {
                line(2, "this." + f.getName() + " = (" + f.getName() + " == null) ? null : " + f.getName() + ".clone();");
            }
            else {
                line(2, "this." + f.getName() + " = " + f.getName() + ";");
            }
        }
        line(1, "}");

        for (Field f : s.getFields().values()) {
            String thisName = "this." + f.getName();
            if (!immutable) {
                line(0, "");
                line(1, "public void set" + f.getUpperName() + "(" + namespace(f.getJavaType()) +
                     " " + f.getName() + ") {");
                line(2, thisName + " = " + f.getName() + ";");
                line(1, "}");
            }

            line(0, "");
            line(1, "public " + namespace(f.getJavaType()) + " get" + f.getUpperName() + "() {");
            if (immutable && f.isArray()) {
                line(2, "return " + thisName + " == null ? null : " + thisName + ".clone();");
            }
            else {
                line(2, "return " + thisName + ";");
            }
            line(1, "}");
        }

        line(0, "");
        line(1, "@Override");
        line(1, "public String toString() {");
        if (hasParent) {
            line(2, "StringBuilder sb = new StringBuilder(super.toString());");
            line(2, "sb.append(\"" + s.getName() + ":\");");
        }
        else {
            line(2, "StringBuilder sb = new StringBuilder(\"" + s.getName() + ":\");");
        }
        for (Field f : s.getFields().values()) {
            if (f.isArray()) {
                line(2, "sb.append(\" " + f.getName() + "=\").append(java.util.Arrays.toString(" + f.getName() + "));");
            }
            else {
                line(2, "sb.append(\" " + f.getName() + "=\").append(" + f.getName() + ");");
            }
        }
        line(2, "return sb.toString();");
        line(1, "}");

        line(0, "");
        line(1, "@Override");
        line(1, "public boolean equals(Object _other) {");
        line(2, "if (this == _other) { return true; }");
        line(2, "if (_other == null) { return false; }");
        line(2, "if (!(_other instanceof " + s.getSimpleName() + ")) { return false; }");
        line(2, s.getSimpleName() + " _o = (" + s.getSimpleName() + ")_other;");
        if (hasParent) {
            line(2, "if (!super.equals(_o)) { return false; }");
        }
        for (Field f : s.getFields().values()) {
            line(2, "if (" + f.getName() + " == null && _o." + f.getName() + " != null) { return false; }");
            if (f.isArray()) {
                line(2, "else if (" + f.getName() + " != null && !java.util.Arrays.equals(" + f.getName() + ", _o." + f.getName() + ")) { return false; }");
            }
            else {
                line(2, "else if (" + f.getName() + " != null && !" + f.getName() + ".equals(_o." + f.getName() + ")) { return false; }");
            }
        }
        line(2, "return true;");
        line(1, "}");

        line(0, "");
        line(1, "@Override");
        line(1, "public int hashCode() {");
        if (hasParent) {
            line(2, "int _hash = super.hashCode();");
        }
        else {
            line(2, "int _hash = 1;");
        }
        for (Field f : s.getFields().values()) {
            if (f.isArray()) {
                line(2, "_hash = _hash * 31 + (" + f.getName() + " == null ? 0 : java.util.Arrays.hashCode(" +
                     f.getName() + "));");
            }
            else {
                line(2, "_hash = _hash * 31 + (" + f.getName() + " == null ? 0 : " +
                     f.getName() + ".hashCode());");
            }
        }
        line(2, "return _hash;");
        line(1, "}");

        line(0, "}");
        toFile(s);
    }

    private void generate(Enum en) throws Exception {
        start(packageFor(en.getName()));
        line(0, "public enum " + en.getSimpleName() + " {");

        StringBuilder vals = new StringBuilder();
        for (String v : en.getValues()) {
            if (vals.length() > 0) {
                vals.append(", ");
            }
            vals.append(v);
        }
        vals.append(";");
        line(1, vals.toString());

        line(0, "");
        line(1, String.format("public static %s[] unmarshalList(Object _obj, boolean _optional) throws com.bitmechanic.barrister.RpcException {", en.getSimpleName()));
        line(2, "if (_obj == null && !_optional) {");
        line(3, String.format("throw com.bitmechanic.barrister.RpcException.Error.INVALID_PARAMS.exc(\"%s list cannot be null\");", en.getSimpleName()));
        line(2, "}");
        line(2, "if (_obj == null) {");
        line(3, "return null;");
        line(2, "}");
        line(2, "else if (_obj instanceof java.util.List) {");
        line(3, "java.util.List _list = (java.util.List)_obj;");
        line(3, String.format("%s[] _arr = new %s[_list.size()];", en.getSimpleName(), en.getSimpleName()));
        line(3, "for (int i = 0; i < _arr.length; i++) {");
        line(4, "_arr[i] = unmarshal(_list.get(i), _optional);");
        line(3, "}");
        line(3, "return _arr;");
        line(2, "}");
        line(2, "else {");
        line(3, String.format("throw com.bitmechanic.barrister.RpcException.Error.INVALID_PARAMS.exc(\"%s.unmarshalList expects List, got: \" + _obj.getClass().getSimpleName());", en.getSimpleName()));
        line(2, "}");
        line(1, "}");

        line(0, "");
        line(1, String.format("public static %s unmarshal(Object _obj, boolean _optional) throws com.bitmechanic.barrister.RpcException {", en.getSimpleName()));
        line(2, "if (_obj == null && !_optional) {");
        line(3, String.format("throw com.bitmechanic.barrister.RpcException.Error.INVALID_PARAMS.exc(\"%s cannot be null\");", en.getSimpleName()));
        line(2, "}");
        line(2, "if (_obj == null) {");
        line(3, "return null;");
        line(2, "}");
        line(2, "else {");
        line(3, "try {");
        line(4, String.format("return %s.valueOf(_obj.toString());", en.getSimpleName()));
        line(3, "}");
        line(3, "catch (Exception _e) {");
        line(4, String.format("throw com.bitmechanic.barrister.RpcException.Error.INVALID_PARAMS.exc(\"String: \" + _obj + \" is not in the %s enum: \" + java.util.Arrays.toString(values()));", en.getSimpleName()));
        line(3, "}");
        line(2, "}");
        line(1, "}");

        line(0, "}");
        toFile(en);
    }

    private void generate(Interface iface) throws Exception {
        String pkgName = packageFor(iface.getName());
        start(pkgName);
        line(0, "");
        line(0, "public interface " + iface.getName() + " {");
        line(0, "");
        for (Function f : iface.getFunctions()) {
            StringBuilder params = new StringBuilder();
            for (Field p : f.getParams()) {
                if (params.length() > 0) {
                    params.append(", ");
                }
                params.append(namespace(p.getJavaType())).append(" ").append(p.getName());
            }

            line(1, "public " + getReturnType(f) + " " +
                 f.getName() + "(" + params + ") throws com.bitmechanic.barrister.RpcException;");
        }
        line(0, "");
        line(0, "}");
        toFile(iface);

        String className = iface.getSimpleName() + "Client";
        start(pkgName);
        line(0, "public class " + className + " implements " + iface.getName() + " {");
        line(0, "");
        line(1, "private com.bitmechanic.barrister.Transport _trans;");
        line(0, "");
        line(1, "public " + className + "(com.bitmechanic.barrister.Transport trans) {");
        line(2, "trans.getContract().setPackage(\"" + pkgName + "\");");
        line(2, "trans.getContract().setNsPackage(\"" + nsPkgName + "\");");
        line(2, "this._trans = trans;");
        line(1, "}");
        for (Function f : iface.getFunctions()) {
            StringBuilder params = new StringBuilder();
            StringBuilder paramNames = new StringBuilder();
            for (Field p : f.getParams()) {
                if (params.length() > 0) {
                    params.append(", ");
                    paramNames.append(", ");
                }
                params.append(namespace(p.getJavaType())).append(" ").append(p.getName());
                paramNames.append(p.getName());
            }

            line(0, "");
            line(1, "public " + getReturnType(f) + " " +
                 f.getName() + "(" + params + ") throws com.bitmechanic.barrister.RpcException {");
            if (f.getParams().size() == 0) {
                line(2, "Object _params = null;");
            }
            else {
                line(2, "Object _params = new Object[] { " + paramNames + " };");
            }
            line(2, "com.bitmechanic.barrister.RpcRequest _req = new com.bitmechanic.barrister.RpcRequest(java.util.UUID.randomUUID().toString(), \"" + iface.getName() + "." + f.getName() + "\", _params);");
            line(2, "com.bitmechanic.barrister.RpcResponse _resp = this._trans.request(_req);");
            if (f.getReturns() == null) {
                line(2, "if (_resp.getError() != null) {");
                line(3, "throw _resp.getError();");
                line(2, "}");
            }
            else {
                line(2, "if (_resp == null) {");
                line(3, "return null;");
                line(2, "}");
                line(2, "else if (_resp.getError() == null) {");
                line(3, "return (" + namespace(f.getReturns().getJavaType()) + ")_resp.getResult();");
                line(2, "}");
                line(2, "else {");
                line(3, "throw _resp.getError();");
                line(2, "}");
            }
            line(1, "}");
        }
        line(0, "");
        line(0, "}");
        toFile(pkgName, className);
    }

    private void start(String pkgName) {
        sb = new StringBuilder();
        line(0, "package " + pkgName + ";");
        line(0, "");
        line(0, "/**");
        line(0, " * DO NOT EDIT THIS FILE!");
        line(0, " * ");
        line(0, " * Generated by Barrister Idl2Java: https://github.com/coopernurse/barrister-java");
        line(0, " * ");
        line(0, " */");
    }

    private void line(int indentLevel, String s) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append("    ");
        }
        sb.append(s);
        sb.append(newline);
    }

    private void toFile(BaseEntity b) throws Exception {
        toFile(packageFor(b.getName()), b.getSimpleName());
    }

    private void toFile(String pkgName, String className) throws Exception {
        String dirName = mkdirForPackage(pkgName);
        String outfile = (dirName + File.separator + className + ".java").replace("/", File.separator);
        out("Writing file: " + outfile);

        PrintWriter w = new PrintWriter(new FileWriter(outfile));
        w.println(sb.toString());
        w.close();
    }

    private String mkdirForPackage(String pkgName) {
        String dirName = (outDir + File.separator + pkgName.replace('.', File.separatorChar)).replace("/", File.separator);
 
        File dir = new File(dirName);
        if (!dir.exists()) {
            out("Creating directory: " + dirName);
            if (!dir.mkdirs()) {
                throw new RuntimeException("Unable to create directory: " + dirName);
            }
        }

        return dirName;
    }

    private String packageFor(String javaType) {
        if (nsPkgName != null && javaType.indexOf(".") > -1) {
            return nsPkgName + "." + javaType.substring(0, javaType.indexOf(".")); 
        }
        else {
            return pkgName;
        }
    }

    private String getReturnType(Function f) {
        return (f.getReturns() == null) ? "void" : namespace(f.getReturns().getJavaType());
    }

    private String namespace(String javaType) {
        if (nsPkgName != null && javaType.indexOf(".") > -1) {
            return nsPkgName + "." + javaType; 
        }

        // built-in types, or non-namespaced types need no prefix
        return javaType;
    }

    private String unmarshalFunc(Field f, String objInstanceCode) {
        String funcCallCode = String.format("(%s, %s)", objInstanceCode, String.valueOf(f.isOptional()));
        if (f.isPrimitive()) {
            String typeConverter = typeConverters.get(f.getType());
            if (f.isArray()) {
                return typeConverter + ".unmarshalList" + funcCallCode;
            }
            else {
                return typeConverter + ".unmarshal" + funcCallCode;
            }
        }
        else if (contract.getEnums().get(f.getType()) != null) {
            if (f.isArray()) {
                return namespace(f.getJavaType(false)) + ".unmarshalList" + funcCallCode;
            }
            else {
                return namespace(f.getJavaType(false)) + ".unmarshal" + funcCallCode;
            }
        }
        else {
            if (f.isArray()) {
                return String.format("com.bitmechanic.barrister.ArrayTypeConverter.unmarshalList(%s.class, %s, %s)", namespace(f.getJavaType(false)), objInstanceCode, String.valueOf(f.isOptional()));
            }
            else {
                return String.format("com.bitmechanic.barrister.StructTypeConverter.unmarshal(%s.class, %s, %s)", namespace(f.getJavaType(false)), objInstanceCode, String.valueOf(f.isOptional()));
            }
        }
    }

    private String join(List<String> list, String delim) {
        StringBuilder sb = new StringBuilder();
        if (list != null) {
            for (String s : list) {
                if (sb.length() > 0) { sb.append(delim); }
                sb.append(s);
            }
        }
        return sb.toString();
    }

    private boolean isImmutable(String className) {
        if (allImmutable) {
            return true;
        }

        for (String s : immutableSubstr) {
            if (!s.isEmpty() && className.contains(s)) {
                return true;
            }
        }

        return false;
    }

}
