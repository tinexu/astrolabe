package main.java.com.codeintel.parser;

import com.codeintel.model.ExtractedSymbol;
import java.util.List;

// interface all language parsers implement
public interface LanguageParser {
    List<ExtractedSymbol> parse(String content);
    String getLanguage();
}