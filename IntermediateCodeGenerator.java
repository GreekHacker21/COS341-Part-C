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
    public LinkedList<String[]> varBasicNames;
    public int aCount, vCount;

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
        varBasicNames = new LinkedList<>();
        aCount = 0;
        vCount = 0;
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

    public String checkIfAdded(String value, int scopeID) {
        for (int i = 0; i < varBasicNames.size(); i++) {
            if (varBasicNames.get(i)[0].equals(value) && Integer.parseInt(varBasicNames.get(i)[1]) == scopeID) {
                return varBasicNames.get(i)[2];
            }
        }
        String varName = "";
        if (value.contains("(")) {
            varName += "a" + aCount;
            aCount++;
        } else {
            varName += "v" + vCount;
            vCount++;
        }
        System.out.println(value);
        String[] add = { value, String.valueOf(scopeID), varName };
        varBasicNames.add(add);
        return varName;
    }

    // public void generateBASICVarNames(){
    // for(int i = 0; i < varBasicNames.size(); i++){
    // if (varBasicNames.get(i)[0].contains("(")){
    // varBasicNames.get(i)[2] = "a" + aCount;
    // aCount++;
    // }else{
    // varBasicNames.get(i)[2] = "v" + vCount;
    // vCount++;
    // }

    // fileOutput += fileOutput.replace(varBasicNames.get(i)[0],
    // varBasicNames.get(i)[2]);
    // }
    // }

    // public void addVarName(String name) {
    // String[] varName = {name, ""};
    // varBasicNames.add(varName);
    // }

    public void generate() {
        // makeArrayDeclarations
        createFile();
        generateCode();
        // generateBASICVarNames();
        // generateProcs();
        // generateMain();
        fixSubProcs();
        generateFile();
    }

    public void fixSubProcs() {
        for (int i = 0; i < procs.size(); i++) {
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
        String[] added = { "MAIN", String.valueOf(lineNumber + 10) };
        // addLine("MAIN:");
        //addLine("");
        for (int i = 0; i < varTable.rows.size(); i++) {
            if (varTable.rows.elementAt(i).isDeclaration && varTable.rows.elementAt(i).scopeID == 0
                    && varTable.rows.elementAt(i).isArray) {
                Node size = new Node("");
                for (int j = 0; j < leafNodes.size(); j++) {
                    if (leafNodes.get(j).id == varTable.rows.elementAt(i).nodeID) {
                        size = leafNodes.get(j - 4);
                    }
                }
                String result = checkIfAdded(varTable.rows.elementAt(i).value + "(",
                        varTable.rows.elementAt(i).scopeID);
                // addLine("DIM " + varTable.rows.elementAt(i).value + "(" + size.value + ")");
                addLine("DIM " + result + "(" + size.value + ")");
            }
        }
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
        String[] added = { n.children.get(1).children.get(0).value, String.valueOf(lineNumber + 10) };
        // addLine(n.children.get(1).children.get(0).value + ":");
        //addLine("");
        for (int i = 0; i < varTable.rows.size(); i++) {
            if (varTable.rows.elementAt(i).isDeclaration && varTable.rows.elementAt(i).scopeID == n.scopeID
                    && varTable.rows.elementAt(i).isArray) {
                Node size = new Node("");
                for (int j = 0; j < leafNodes.size(); j++) {
                    if (leafNodes.get(j).id == varTable.rows.elementAt(i).nodeID) {
                        size = leafNodes.get(j - 4);
                    }
                }
                addLine("DIM " + varTable.rows.elementAt(i).value + "(" + size.value + ")");
            }
        }
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
        String result;
        switch (lhs.children.get(0).value) {
            case "output":
                assignLine += "PRINT ";
                break;
            case "Var":
                s = "";
                if (checkTypeFromNodeID(lhs.children.get(0).children.get(0).children.get(0).id).equals("S")) {
                    s = "$";
                }
                result = checkIfAdded(lhs.children.get(0).children.get(0).children.get(0).value + "",
                        lhs.children.get(0).children.get(0).children.get(0).scopeID);
                // assignLine += "LET " +
                // lhs.children.get(0).children.get(0).children.get(0).value + s + " = ";
                assignLine += "LET " + result + s + " = ";
                break;
            case "Field":
                String field = "LET ";
                s = "";
                if (checkTypeFromNodeID(lhs.children.get(0).children.get(0).children.get(0).id).equals("S")) {
                    s = "$";
                }
                result = checkIfAdded(lhs.children.get(0).children.get(0).children.get(0).value + "(",
                        lhs.children.get(0).children.get(0).children.get(0).scopeID);
                field = result + s;
                if (lhs.children.get(0).children.get(2).value.equals("Const")) {
                    field += "(" + lhs.children.get(0).children.get(2).children.get(0).value + ")";
                } else {
                    String temp = checkIfAdded(lhs.children.get(0).children.get(2).children.get(0).children.get(0).value,
                    lhs.children.get(0).children.get(2).children.get(0).children.get(0).scopeID);
                    field += "(" + temp + ")";
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
        String result;
        switch (n.value) {
            case "Const":
                return n.children.get(0).value;
            case "Var":
                s = "";
                if (checkTypeFromNodeID(n.children.get(0).children.get(0).id).equals("S")) {
                    s = "$";
                }
                result = checkIfAdded(n.children.get(0).children.get(0).value + "",
                        n.children.get(0).children.get(0).scopeID);
                // return n.children.get(0).value + s;
                return result + s;

            case "Field":
                s = "";
                if (checkTypeFromNodeID(n.children.get(0).children.get(0).id).equals("S")) {
                    s = "$";
                }
                result = checkIfAdded(n.children.get(0).children.get(0).value + "(",
                        n.children.get(0).children.get(0).scopeID);
                //System.out.println(result);
                String field = "";
                // field = n.children.get(0).children.get(0).value;
                field = result + s;
                if (n.children.get(2).value.equals("Const")) {
                    field += "(" + n.children.get(2).children.get(0).value + ")";
                } else {
                    String temp = checkIfAdded(n.children.get(2).children.get(0).children.get(0).value,
                            n.children.get(2).children.get(0).children.get(0).scopeID);
                    field += "(" + temp + ")";
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
        String expr1 = Expr(n.children.get(2), level + 1);
        String expr2 = Expr(n.children.get(4), level + 1);
        switch (n.children.get(0).value) {
            case "and":
                addLine("LET A" + level + " = 0");
                addLine("IF (" + expr1 + ") THEN GOTO " + String.valueOf(lineNumber + 30));
                addLine("GOTO " + String.valueOf(lineNumber + 30));
                addLine("IF (" + expr2 + ") THEN GOTO " + String.valueOf(lineNumber + 30));
                addLine("GOTO " + String.valueOf(lineNumber + 30));
                addLine("LET A" + level + " = 1");
                return "A" + level;
            case "or":
                addLine("LET O" + level + " = 0");
                addLine("IF (" + expr1 + ") THEN GOTO " + String.valueOf(lineNumber + 50));
                addLine("GOTO " + String.valueOf(lineNumber + 30));
                addLine("IF (" + expr2 + ") THEN GOTO " + String.valueOf(lineNumber + 30));
                addLine("GOTO " + String.valueOf(lineNumber + 30));
                addLine("LET O" + level + " = 1");
                return "O" + level;
            case "eq":
                addLine("LET B" + level + " = " + expr1 + "=" + expr2);
                // return expr1 + "=" + expr2;
                return "B" + level;
            case "larger":
                addLine("LET B" + level + " = " + expr1 + ">" + expr2);
                return "B" + level;
            case "add":
                addLine("LET B" + level + " = " + expr1 + "+" + expr2);
                return "B" + level;
            case "sub":
                addLine("LET B" + level + " = " + expr1 + "-" + expr2);
                return "B" + level;
            case "mult":
                addLine("LET B" + level + " = " + expr1 + "*" + expr2);
                return "B" + level;
        }
        return "";
    }

    // UnOp → input(Var)
    // UnOp → not(Expr)
    public String UnOp(Node n, int level) {
        if (n.children.get(0).value.equals("input")) {
            String s = "";
            System.out.println(n.children.get(2).children.get(0).children.get(0).value);
            System.out.println(n.children.get(2).children.get(0).children.get(0).id);
            System.out.println(checkTypeFromNodeID(n.children.get(2).children.get(0).children.get(0).id));
            if (checkTypeFromNodeID(n.children.get(2).children.get(0).children.get(0).id).equals("S")) {
                s = "$";
                System.out.println("$");
            }
            String result = checkIfAdded(n.children.get(2).children.get(0).children.get(0).value,
                    n.children.get(2).children.get(0).children.get(0).scopeID);
            addLine("INPUT " + result + s);
            return result + s;

        }
        addLine("LET U" + level + " = 0");
        addLine("IF (UNOPREPLACE1) THEN GOTO UNOPREPLACE2");
        addLine("GOTO UNOPREPLACE3");
        String result = Expr(n.children.get(2), level + 1);
        addLine("LET U" + level + " = 1");
        fileOutput = fileOutput.replace("UNOPREPLACE1", result);
        fileOutput = fileOutput.replace("UNOPREPLACE3", String.valueOf(lineNumber));
        fileOutput = fileOutput.replace("UNOPREPLACE2", String.valueOf(lineNumber + 10));

        return "U" + level;
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
