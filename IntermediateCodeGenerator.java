import java.util.LinkedList;

public class IntermediateCodeGenerator {

    private Node SyntaxTree;
    private ScopeNode scopeInfo;
    private LinkedList<Node> leafNodes;
    SymbolTable procTable;
    SymbolTable varTable;
    String fileOutput;
    int lineNumber;

    
    IntermediateCodeGenerator(Node sT, LinkedList<Node> lN, ScopeNode scopeInfo, SymbolTable pT, SymbolTable vT){
        this.SyntaxTree = sT;
        this.leafNodes = lN;
        this.scopeInfo = scopeInfo;
        this.procTable = pT;
        this.varTable = vT;
        fileOutput = "";
        lineNumber = 0;
    }

    public void addLine(String instruction){
        lineNumber += 10;
        fileOutput += lineNumber + " " + instruction + "\n";
    }

    public void generate(){
        
        

    }

}
