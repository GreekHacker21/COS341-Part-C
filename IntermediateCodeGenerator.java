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

        createFile();
        generateCode();
        //generateProcs();
        //generateMain();
        generateFile();
    }

    public void generateCode(){
        for(int i = 0; i < SyntaxTree.children.size(); i++){
            if(SyntaxTree.children.get(i).value.equals("ProcDefs")){
                ProcDefs(SyntaxTree.children.get(i));
            }
        }
    }

    public void ProcDefs(Node n){
        
    }


    /*
    public void generateProcs() {

    }

    public void generateMain() {

        addLine("MAIN:");
        int startElement = 0;
        for (int i = 0; i < leafNodes.size(); i++) {
            if (leafNodes.get(i).getScopeID() == 0) {
                startElement = i;
            }
        }
        LinkedList<Integer> algorithmIndexes = new LinkedList<Integer>();
        for (int i = startElement; i < leafNodes.size(); i++) {
            if (leafNodes.get(i).value.equals("Algorithm")) {
                algorithmIndexes.add(i);
            }
        }
        for (int i = 0; i < algorithmIndexes.size(); i++) {
            switch (leafNodes.get(algorithmIndexes.get(i) + 2).value) {
                case "Assign":
                    switch (leafNodes.get(algorithmIndexes.get(i) + 4).value) {
                        case "output":
                            addLine("PRINT " + leafNodes.get(algorithmIndexes.get(i) + 6).value);
                            break;
                        case "Var":
                            break;
                        case "Field":
                            break;
                    }
                    break;
                case "Branch":

                    break;
                case "Loop":

                    break;
                case "PCall":
                    addLine("GOSUB " + leafNodes.get(algorithmIndexes.get(i) + 3).value);
                    break;
            }
        }
        addLine("END");
    }
    */

}
