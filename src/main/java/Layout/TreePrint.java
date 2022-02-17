package Layout;

public class TreePrint {
    static int indent = 0;

    public static void println(String msg) {
        StringBuilder indentStr = new StringBuilder();
        for(int i = 0; i < indent; i++) {
            indentStr.append("\t");
        }

        System.out.println(indentStr.toString() + msg);
    }

    public static void indent(){
        indent++;
    }

    public static void unindent(){
        indent--;
    }

}
