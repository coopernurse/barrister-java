package com.bitmechanic.barrister;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
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
        }

        if (isBlank(idlFile) || isBlank(pkgName) || isBlank(outDir)) {
            out("Usage: java com.bitmechanic.barrister.Idl2Java -j [idl file] -p [Java package name] -o [out dir]");
            System.exit(1);
        }

        new Idl2Java(idlFile, pkgName, outDir);
    }

    private static boolean isBlank(String s) {
        return (s == null) || (s.trim().equals(""));
    }

    private static void out(String msg) {
        System.out.println(msg);
    }

    ///////////////////////////

    static String newline = System.getProperty("line.separator");

    private String dirName;
    private String pkgName;

    private Contract contract;
    private StringBuilder sb;
    
    public Idl2Java(String idlJson, String pkgName, String outDir) throws Exception {
        out("Reading IDL from: " + idlJson);
        contract = Contract.load(new File(idlJson));

        out("Using package name: " + pkgName);
        this.pkgName = pkgName;        

        dirName = outDir + File.separator + pkgName.replace('.', File.separatorChar);
 
        File dir = new File(dirName);
        if (!dir.exists()) {
            out("Creating directory: " + dirName);
            if (!dir.mkdirs()) {
                throw new Exception("Unable to create: " + dirName);
            }
        }

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
        start();
        line(0, "public class BarristerMeta {");
        line(0, "");
        for (String key : meta.keySet()) {
            Object val = meta.get(key);
            String type = val.getClass().getSimpleName();
            if (val.getClass() == Long.class) {
              val = val + "L";
            }
            else if (val.getClass() == String.class) {
                val = "\"" + val + "\"";
            }
            line(1, "public static final " + type + " " + key.toUpperCase() + " = " + val + ";");
        }
        line(0, "");
        line(0, "}");
        toFile("BarristerMeta");
    }

    private void generate(Struct s) throws Exception {
        start();
        boolean hasParent = false;
        String extend = " implements com.bitmechanic.barrister.BStruct";
        if (!isBlank(s.getExtends())) {
            hasParent = true;
            extend = " extends " + s.getExtends();
        }
        line(0, "public class " + s.getName() + extend + " {");

        line(0, "");
        for (Field f : s.getFields().values()) {
            line(1, "private " + f.getJavaType() + " " + f.getName() + ";");
        }

        Map<String,Field> allFields = s.getFieldsPlusParents();
        line(0, "");
        line(1, "public static class Builder {");
        for (String name : allFields.keySet()) {
            Field f = allFields.get(name);
            line(2, "private " + f.getJavaType() + " _" + f.getName() + ";");
            line(2, "public Builder " + f.getName() + "(" + f.getJavaType() + 
                 " " + f.getName() + ") { " +
                 "this._" + f.getName() + " = " + f.getName() + "; return this; }");
        }
        line(2, "public " + s.getName() + " build() {");
        line(3, s.getName() + " _tmp = new " + s.getName() + "();");
        for (String name : allFields.keySet()) {
            Field f = allFields.get(name);
            line(3, "_tmp.set" + f.getUpperName() + "(_" + f.getName() + ");");
        }
        line(3, "return _tmp;");
        line(2, "}");
        line(1, "}");

        for (Field f : s.getFields().values()) {
            line(0, "");
            line(1, "public void set" + f.getUpperName() + "(" + f.getJavaType() + 
                 " " + f.getName() + ") {");
            line(2, "this." + f.getName() + " = " + f.getName() + ";");
            line(1, "}");

            line(0, "");
            line(1, "public " + f.getJavaType() + " get" + f.getUpperName() + "() {");
            line(2, "return this." + f.getName() + ";");
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
            line(2, "sb.append(\" " + f.getName() + "=\").append(" + f.getName() + ");");
        }
        line(2, "return sb.toString();");
        line(1, "}");

        line(0, "");
        line(1, "@Override");
        line(1, "public boolean equals(Object _other) {");
        line(2, "if (this == _other) { return true; }");
        line(2, "if (_other == null) { return false; }");
        line(2, "if (!(_other instanceof " + s.getName() + ")) { return false; }");
        line(2, s.getName() + " _o = (" + s.getName() + ")_other;");
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
        start();
        line(0, "public enum " + en.getName() + " {");

        StringBuilder vals = new StringBuilder();
        for (String v : en.getValues()) {
            if (vals.length() > 0) {
                vals.append(", ");
            }
            vals.append(v);
        }
        vals.append(";");
        line(1, vals.toString());

        line(0, "}");
        toFile(en);
    }

    private void generate(Interface iface) throws Exception {
        start();
        line(0, "");
        line(0, "public interface " + iface.getName() + " {");
        line(0, "");
        for (Function f : iface.getFunctions()) {
            StringBuilder params = new StringBuilder();
            for (Field p : f.getParams()) {
                if (params.length() > 0) {
                    params.append(", ");
                }
                params.append(p.getJavaType()).append(" ").append(p.getName());
            }

            line(1, "public " + f.getReturns().getJavaType() + " " +
                 f.getName() + "(" + params + ") throws com.bitmechanic.barrister.RpcException;");
        }
        line(0, "");
        line(0, "}");
        toFile(iface);

        String className = iface.getName() + "Client";
        start();
        line(0, "public class " + className + " implements " + iface.getName() + " {");
        line(0, "");
        line(1, "private com.bitmechanic.barrister.Transport _trans;");
        line(0, "");
        line(1, "public " + className + "(com.bitmechanic.barrister.Transport trans) {");
        line(2, "trans.getContract().setPackage(\"" + pkgName + "\");");
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
                params.append(p.getJavaType()).append(" ").append(p.getName());
                paramNames.append(p.getName());
            }

            line(0, "");
            line(1, "public " + f.getReturns().getJavaType() + " " +
                 f.getName() + "(" + params + ") throws com.bitmechanic.barrister.RpcException {");
            if (f.getParams().size() == 0) {
                line(2, "Object _params = null;");
            }
            else {
                line(2, "Object _params = new Object[] { " + paramNames + " };");
            }
            line(2, "com.bitmechanic.barrister.RpcRequest _req = new com.bitmechanic.barrister.RpcRequest(java.util.UUID.randomUUID().toString(), \"" + iface.getName() + "." + f.getName() + "\", _params);");
            line(2, "com.bitmechanic.barrister.RpcResponse _resp = this._trans.request(_req);");
            line(2, "if (_resp == null) {");
            line(3, "return null;");
            line(2, "}");
            line(2, "else if (_resp.getError() == null) {");
            line(3, "return (" + f.getReturns().getJavaType() + ")_resp.getResult();");
            line(2, "}");
            line(2, "else {");
            line(3, "throw _resp.getError();");
            line(2, "}");
            line(1, "}");
        }
        line(0, "");
        line(0, "}");
        toFile(className);
    }

    private void start() {
        sb = new StringBuilder();
        line(0, "package " + pkgName + ";");
        line(0, "");
        line(0, "/**");
        line(0, " * DO NOT EDIT THIS FILE!");
        line(0, " * ");
        line(0, " * Generated by Barrister Idl2Java: https://github.com/coopernurse/barrister-java");
        line(0, " * ");
        line(0, " * Generated at: " + new java.util.Date());
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
        toFile(b.getName());
    }

    private void toFile(String className) throws Exception {
        String outfile = dirName + File.separator + className + ".java";
        out("Writing file: " + outfile);

        PrintWriter w = new PrintWriter(new FileWriter(outfile));
        w.println(sb.toString());
        w.close();
    }

}