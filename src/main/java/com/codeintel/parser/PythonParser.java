package main.java.com.codeintel.parser;

import com.codeintel.model.ExtractedSymbol;
import com.codeintel.model.SymbolType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PythonParser implements LanguageParser {

    // matches class MyClass, class MyClass(BaseClass)
    private static final Pattern CLASS_PATTERN = Pattern.compile(
        "^class\\s+(\\w+)\\s*[:(]",
        Pattern.MULTILINE
    );

    // matches def my_function(...), async def my_function(...)
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
        "^\\s*(?:async\\s+)?def\\s+(\\w+)\\s*\\(",
        Pattern.MULTILINE
    );

    // matches import os, from os.path import join
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
        "^\\s*(?:from\\s+([\\w.]+)\\s+)?import\\s+([\\w.,\\s*]+)",
        Pattern.MULTILINE
    );

    // matches @decorator, @app.route("/")
    private static final Pattern DECORATOR_PATTERN = Pattern.compile(
        "^\\s*(@\\w[\\w.]*)",
        Pattern.MULTILINE
    );

    @Override
    public List<ExtractedSymbol> parse(String content) {
        List<ExtractedSymbol> symbols = new ArrayList<>();
        String[] lines = content.split("\n");

        extractWithPattern(content, lines, CLASS_PATTERN, SymbolType.CLASS, symbols);
        extractWithPattern(content, lines, FUNCTION_PATTERN, SymbolType.FUNCTION, symbols);
        extractWithPattern(content, lines, DECORATOR_PATTERN, SymbolType.ANNOTATION, symbols);

        // imports need special handling: group(1) is the "from" module, group(2) is the import
        Matcher importMatcher = IMPORT_PATTERN.matcher(content);
        while (importMatcher.find()) {
            String fromModule = importMatcher.group(1);
            String imported = importMatcher.group(2);
            String name = (fromModule != null) ? fromModule + "." + imported.trim() : imported.trim();
            int lineNumber = getLineNumber(content, importMatcher.start());

            symbols.add(new ExtractedSymbol(
                name, SymbolType.IMPORT, lineNumber, lines[lineNumber - 1].trim()
            ));
        }

        return symbols;
    }

    private void extractWithPattern(String content, String[] lines, Pattern pattern,
                                     SymbolType type, List<ExtractedSymbol> symbols) {
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String name = matcher.group(1);
            int lineNumber = getLineNumber(content, matcher.start());
            String rawSignature = lines[lineNumber - 1].trim();
            symbols.add(new ExtractedSymbol(name, type, lineNumber, rawSignature));
        }
    }

    private int getLineNumber(String content, int charIndex) {
        int line = 1;
        for (int i = 0; i < charIndex && i < content.length(); i++) {
            if (content.charAt(i) == '\n') line++;
        }
        return line;
    }

    @Override
    public String getLanguage() {
        return "python";
    }
}