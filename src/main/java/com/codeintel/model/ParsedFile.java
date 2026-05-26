package main.java.com.codeintel.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParsedFile {
    private UUID repoId;
    private String repoName;
    private String filePath;
    private String language;
    private String rawContent;
    private int totalLines;
    private long fileSizeBytes;
    private List<ExtractedSymbol> symbols = new ArrayList<>();

    public ParsedFile() {}

    // convenience accessors for specific symbol types
    public List<ExtractedSymbol> getSymbolsByType(SymbolType type) {
        return symbols.stream()
            .filter(s -> s.getType() == type)
            .toList();
    }

    public int getSymbolCount() {
        return symbols.size();
    }

    // getters and setters
    public UUID getRepoId() { return repoId; }
    public void setRepoId(UUID repoId) { this.repoId = repoId; }
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getRawContent() { return rawContent; }
    public void setRawContent(String rawContent) { this.rawContent = rawContent; }
    public int getTotalLines() { return totalLines; }
    public void setTotalLines(int totalLines) { this.totalLines = totalLines; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public List<ExtractedSymbol> getSymbols() { return symbols; }
    public void setSymbols(List<ExtractedSymbol> symbols) { this.symbols = symbols; }
}