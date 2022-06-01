import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

public class IntermediateCodeGenerator {

    private Node SyntaxTree;
    private ScopeNode scopeInfo;
    private LinkedList<Node> leafNodes;
    SymbolTable procTable;
    SymbolTable varTable;
    String fileOutput;
    int lineNumber;
    String fileName;
    public LinkedList<String[]> procs;

    IntermediateCodeGenerator(Node sT, LinkedList<Node> lN, ScopeNode scopeInfo, SymbolTable pT, SymbolTable vT,
            String fN) {
        this.SyntaxTree = sT;
        this.leafNodes = lN;
        this.scopeInfo = scopeInfo;
        this.procTable = pT;
        this.varTable = vT;
        fileOutput = "";
        lineNumber = 0;
        fileName = fN;
        procs = new LinkedList<>();
    }

    public void addLine(String instruction) {
        lineNumber += 10;
        fileOutput += lineNumber + " " + instruction + "\n";
    }

    public void generateFile() {
        try {
            File file = new File(fileName + ".bas");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileOutput.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createFile() {
        File file = new File(fileName + ".bas"); // initialize File object and passing path as argument
        boolean result;
        try {
            result = file.createNewFile(); // creates a new file
            if (!result) // test if successfully created a new file
            {
                FileOutputStream fos = new FileOutputStream(fileName + ".bas", true); // true for append mode
                String empty = "";
                byte[] b = empty.getBytes(); // converts string into bytes
                fos.write(b); // writes bytes into file
                fos.close(); // close the file
            }
        } catch (IOException e) {
            e.printStackTrace(); // prints exception if any
        }
        addLine("GOTO MAIN");
    }

    public void generate() {
        //makeArrayDeclarations
        createFile();
        generateCode();
        // generateProcs();
        // generateMain();
        fixSubProcs();
        generateFile();
    }

    public void fixSubProcs() {
        for (int i = 0; i < procs.size(); i++) {
            System.out.println(procs.get(i)[0] + "\t" + procs.get(i)[1]);
            fileOutput = fileOutput.replaceAll("GOSUB " + procs.get(i)[0], "GOSUB " + procs.get(i)[1]);
            fileOutput = fileOutput.replaceAll("GOTO " + procs.get(i)[0], "GOTO " + procs.get(i)[1]);
        }
        fileOutput = fileOutput.toUpperCase(); // uppercase everything
        fileOutput = fileOutput.replaceAll("TRUE", "1"); // true into 1
        fileOutput = fileOutput.replaceAll("FALSE", "0"); // false into 0
    }

    public void generateCode() {
        for (int i = 0; i < SyntaxTree.children.size(); i++) {
            if (SyntaxTree.children.get(i).value.equals("ProcDefs")) {
                ProcDefs(SyntaxTree.children.get(i));
            }
        }
        // addLine("MAIN:");
        addLine("");
        for(int i = 0; i < varTable.rows.size(); i++){
            if(varTable.rows.elementAt(i).isDeclaration&&varTable.rows.elementAt(i).scopeID==0&&varTable.rows.elementAt(i).isArray){
                Node size = new Node("");
                for(int j = 0 ; j < leafNodes.size(); j++){
                    if(leafNodes.get(j).id==varTable.rows.elementAt(i).nodeID){
                        size = leafNodes.get(j-4);
                    }
                }
                addLine("DIM " + varTable.rows.elementAt(i).value + "(" + size.value + ")");
            }
        }
        String[] added = { "MAIN", String.valueOf(lineNumber + 10) };
        procs.add(added);
        for (int i = 0; i < SyntaxTree.children.size(); i++) {
            if (SyntaxTree.children.get(i).value.equals("Algorithm")) {
                Algorithm(SyntaxTree.children.get(i));
            }
        }
        addLine("END");
    }

    public void ProcDefs(Node n) {
        n = n.children.get(0);
        // addLine(n.children.get(1).children.get(0).value + ":");
        addLine("");
        for(int i = 0; i < varTable.rows.size(); i++){
            if(varTable.rows.elementAt(i).isDeclaration&&varTable.rows.elementAt(i).scopeID==n.scopeID&&varTable.rows.elementAt(i).isArray){
                Node size = new Node("");
                for(int j = 0 ; j < leafNodes.size(); j++){
                    if(leafNodes.get(j).id==varTable.rows.elementAt(i).nodeID){
                        size = leafNodes.get(j-4);
                    }
                }
                addLine("DIM " + varTable.rows.elementAt(i).value + "(" + size.value + ")");
            }
        }
        String[] added = { n.children.get(1).children.get(0).value, String.valueOf(lineNumber + 10) };
        procs.add(added);
        for (int i = 0; i < n.children.size(); i++) {
            if (n.children.get(i).value.equals("ProcDefs")) {
                ProcDefs(n.children.get(i));
            }
        }
        for (int i = 0; i < n.children.size(); i++) {
            if (n.children.get(i).value.equals("Algorithm")) {
                Algorithm(n.children.get(i));
            }
        }
        addLine("RETURN");
    }

    public void Algorithm(Node n) {
        n = n.children.get(0);
        n = n.children.get(0);
        switch (n.value) {
            case "Assign":
                Assign(n);
                break;
            case "Branch":
                Branch(n);
                break;
            case "Loop":
                Loop(n);
                break;
            case "PCall":
                PCall(n);
                break;
        }
    }

    // Assign → LHS := Expr
    // LHS → output
    // LHS → Var
    // LHS → Field
    public void Assign(Node n) {
        String assignLine = "";
        Node lhs = n.children.get(0);
        Node expr = n.children.get(2);
        String s;
        switch (lhs.children.get(0).value) {
            case "output":
                assignLine += "PRINT ";
                break;
            case "Var":
                s = "";
                if (checkTypeFromNodeID(lhs.children.get(0).children.get(0).children.get(0).id) == "S") {
                    s = "$";
                }
                assignLine += "LET " + lhs.children.get(0).children.get(0).children.get(0).value + s + " = ";
                break;
            case "Field":
                String field = "LET ";
                s = "";
                if (checkTypeFromNodeID(lhs.children.get(0).children.get(0).children.get(0).id) == "S") {
                    s = "$";
                }
                field = lhs.children.get(0).children.get(0).children.get(0).value + s;
                if (lhs.children.get(0).children.get(2).value.equals("Const")) {
                    field += "(" + lhs.children.get(0).children.get(2).children.get(0).value + ")";
                } else {
                    field += "(" + lhs.children.get(0).children.get(2).children.get(0).children.get(0).value + ")";
                }
                assignLine += field + " = ";
                break;
        }
        assignLine += Expr(expr, 0);
        addLine(assignLine);
    }

    // Branch → if (Expr) then { Algorithm } Alternat
    public void Branch(Node n) {
        int currLine = lineNumber;
        Node expr = n.children.get(2);
        String result = Expr(expr, 0);
        String assignLine = "IF (" + result + ") THEN GOTO REPLACE1" + currLine;
        addLine(assignLine);

        for (int i = 0; i < n.children.size(); i++) {
            if (n.children.get(i).value.equals("Alternat")) {
                Alternat(n.children.get(i));
            }
        }
        addLine("GOTO REPLACE2" + currLine);
        fileOutput = fileOutput.replace("REPLACE1" + currLine, lineNumber + 10 + "");

        for (int i = 0; i < n.children.size(); i++) {
            if (n.children.get(i).value.equals("Algorithm")) {
                Algorithm(n.children.get(i));
            }
        }
        fileOutput = fileOutput.replace("REPLACE2" + currLine, lineNumber + 10 + "");

    }

    public void Alternat(Node n) {
        for (int i = 0; i < n.children.size(); i++) {
            if (n.children.get(i).value.equals("Algorithm")) {
                Algorithm(n.children.get(i));
            }
        }
    }

    // Loop → do { Algorithm } until (Expr)
    // Loop → while (Expr) do { Algorithm }
    public void Loop(Node n) {
        int currLine = lineNumber;
        if (n.children.get(0).value.equals("do")) {
            int beginLine = lineNumber + 10;
            for (int i = 0; i < n.children.size(); i++) {
                if (n.children.get(i).value.equals("Algorithm")) {
                    Algorithm(n.children.get(i));
                }
            }
            Node expr = new Node("");
            for (int i = 0; i < n.children.size(); i++) {
                if (n.children.get(i).value.equals("Expr")) {
                    expr = n.children.get(i);
                }
            }
            String result = Expr(expr, 0);
            addLine("IF (" + result + ") THEN GOTO " + beginLine);

        } else {
            int beginLine = lineNumber + 10;
            Node expr = new Node("");
            for (int i = 0; i < n.children.size(); i++) {
                if (n.children.get(i).value.equals("Expr")) {
                    expr = n.children.get(i);
                }
            }
            String result = Expr(expr, 0);
            addLine("IF (" + result + ") THEN GOTO " + String.valueOf(beginLine + 20));
            addLine("GOTO REPLACE2" + currLine);
            for (int i = 0; i < n.children.size(); i++) {
                if (n.children.get(i).value.equals("Algorithm")) {
                    Algorithm(n.children.get(i));
                }
            }
            addLine("GOTO " + beginLine);
            fileOutput = fileOutput.replace("REPLACE2" + currLine, String.valueOf(lineNumber + 10));

        }

    }

    // Expr → Const
    // Expr → Var
    // Expr → Field
    // Expr → UnOp
    // Expr → BinOp
    public String Expr(Node n, int level) {
        n = n.children.get(0);
        String s;
        switch (n.value) {
            case "Const":
                return n.children.get(0).value;
            case "Var":
                s = "";
                if (checkTypeFromNodeID(n.children.get(0).children.get(0).id) == "S") {
                    s = "$";
                }
                if (level == 0) {
                    return n.children.get(0).value + s;
                } else {
                    return n.children.get(0).value + level + s;
                }
            case "Field":
                s = "";
                if (checkTypeFromNodeID(n.children.get(0).children.get(0).id) == "S") {
                    s = "$";
                }
                String field = "";
                if (level == 0) {
                    field = n.children.get(0).children.get(0).value;
                } else {
                    field = n.children.get(0).children.get(0).value + level;
                }
                if (n.children.get(0).value.equals("Const")) {
                    field += s + "(" + n.children.get(0).children.get(0).value + ")";
                } else {
                    field += s + "(" + n.children.get(0).children.get(0).children.get(0).value + ")";
                }
                return field;
            case "BinOp":
                return BinOp(n, level);
            case "UnOp":
                return UnOp(n, level);
        }
        return "";
    }

    // BinOp → and(Expr,Expr)
    // BinOp → or(Expr,Expr)
    // BinOp → eq(Expr,Expr)
    // BinOp → larger(Expr,Expr)
    // BinOp → add(Expr,Expr)
    // BinOp → sub(Expr,Expr)
    // BinOp → mult(Expr,Expr)
    public String BinOp(Node n, int level) {
        return "";
    }

    // UnOp → input(Var)
    // UnOp → not(Expr)
    public String UnOp(Node n, int level) {
        if (n.children.get(0).value.equals("input")) {
            String s = "";
            if (checkTypeFromNodeID(n.children.get(2).children.get(0).children.get(0).id) == "S") {
                s = "$";
            }
        }
        return "";
    }

    // PCall → call userDefinedName
    public void PCall(Node n) {
        addLine("GOSUB " + n.children.get(1).children.get(0).value);
    }

    public String checkTypeFromNodeID(int nodeID) {
        int check = 0;
        for (int i = 0; i < varTable.rows.size(); i++) {
            if (varTable.rows.elementAt(i).nodeID == nodeID) {
                if (varTable.rows.elementAt(i).isDeclaration) {
                    return varTable.rows.elementAt(i).type;
                } else {
                    check = varTable.rows.elementAt(i).varLinkNodeID;
                    i = varTable.rows.size();
                }
            }
        }
        for (int i = 0; i < varTable.rows.size(); i++) {
            if (varTable.rows.elementAt(i).nodeID == check) {
                return varTable.rows.elementAt(i).type;
            }
        }
        return "";
    }

    /*
     * public void generateProcs() {
     * 
     * }
     * 
     * public void generateMain() {
     * 
     * addLine("MAIN:");
     * int startElement = 0;
     * for (int i = 0; i < leafNodes.size(); i++) {
     * if (leafNodes.get(i).getScopeID() == 0) {
     * startElement = i;
     * }
     * }
     * LinkedList<Integer> algorithmIndexes = new LinkedList<Integer>();
     * for (int i = startElement; i < leafNodes.size(); i++) {
     * if (leafNodes.get(i).value.equals("Algorithm")) {
     * algorithmIndexes.add(i);
     * }
     * }
     * for (int i = 0; i < algorithmIndexes.size(); i++) {
     * switch (leafNodes.get(algorithmIndexes.get(i) + 2).value) {
     * case "Assign":
     * switch (leafNodes.get(algorithmIndexes.get(i) + 4).value) {
     * case "output":
     * addLine("PRINT " + leafNodes.get(algorithmIndexes.get(i) + 6).value);
     * break;
     * case "Var":
     * break;
     * case "Field":
     * break;
     * }
     * break;
     * case "Branch":
     * 
     * break;
     * case "Loop":
     * 
     * break;
     * case "PCall":
     * addLine("GOSUB " + leafNodes.get(algorithmIndexes.get(i) + 3).value);
     * break;
     * }
     * }
     * addLine("END");
     * }
     */

}
