package main.java.com.codeintel.model;

public class ExtractedSymbol {
    private String name;
    private SymbolType type;
    private int lineNumber;
    private String rawSignature;  // full line/block that declared this symbol

    public ExtractedSymbol() {}

    public ExtractedSymbol(String name, SymbolType type, int lineNumber, String rawSignature) {
        this.name = name;
        this.type = type;
        this.lineNumber = lineNumber;
        this.rawSignature = rawSignature;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SymbolType getType() { return type; }
    public void setType(SymbolType type) { this.type = type; }
    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
    public String getRawSignature() { return rawSignature; }
    public void setRawSignature(String rawSignature) { this.rawSignature = rawSignature; }
}