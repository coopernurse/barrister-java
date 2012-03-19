package com.bitmechanic.barrister;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;

public class Idl2Java {

    public static void main(String argv[]) throws Exception {
        String idlFile = null;
        String pkgName = null;
        String outDir = null;

        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-i")) {
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
            out("Usage: idl2java.sh -i [idl file] -p [Java package name] -o [out dir]");
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

    private StringBuilder sb;
    
    public Idl2Java(String idlJson, String pkgName, String outDir) throws Exception {
        out("Reading IDL from: " + idlJson);
        Contract c = Contract.load(new File(idlJson));

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

        for (Struct s : c.getStructs().values()) {
            generate(s);
        }

        for (Enum e : c.getEnums().values()) {
            generate(e);
        }

        for (Interface i : c.getInterfaces().values()) {
            generate(i);
        }
    }

    private void generate(Struct s) throws Exception {
        start(s);
        line(0, "public class " + s.getName() + " {");

        for (Field f : s.getFields().values()) {
            line(1, "private " + f.getJavaType() + " " + f.getName() + ";");
        }

        for (Field f : s.getFields().values()) {
            line(0, "");
            line(1, "public void set" + f.getUpperName() + "(" + f.getJavaType() + 
                 " " + f.getName() + ") {");
            line(2, "this." + f.getName() + " = " + f.getName() + ";");
            line(1, "}");
        }

        line(0, "}");
        toFile(s);
    }

    private void generate(Enum en) throws Exception {
        start(en);
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
        start(iface);
        line(0, "import com.bitmechanic.barrister.RpcException;");
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
                 f.getName() + "(" + params + ") throws RpcException;");
        }
        line(0, "");
        line(0, "}");
        toFile(iface);
    }

    private void start(BaseEntity b) {
        sb = new StringBuilder();
        line(0, "package " + pkgName + ";");
        line(0, "");
    }

    private void line(int indentLevel, String s) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append("    ");
        }
        sb.append(s);
        sb.append(newline);
    }

    private void toFile(BaseEntity b) throws Exception {
        String outfile = dirName + File.separator + b.getName() + ".java";
        out("Writing file: " + outfile);

        PrintWriter w = new PrintWriter(new FileWriter(outfile));
        w.println(sb.toString());
        w.close();
    }

}