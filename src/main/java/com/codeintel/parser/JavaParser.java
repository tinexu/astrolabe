package main.java.com.codeintel.parser;

import com.codeintel.model.ExtractedSymbol;
import com.codeintel.model.SymbolType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaParser implements LanguageParser {

    // matches public class Foo, abstract class Bar, class Baz
    private static final Pattern CLASS_PATTERN = Pattern.compile(
        "^\\s*(?:public|private|protected)?\\s*(?:abstract|final)?\\s*class\\s+(\\w+)",
        Pattern.MULTILINE
    );

    // matches public interface Foo, interface Bar
    private static final Pattern INTERFACE_PATTERN = Pattern.compile(
        "^\\s*(?:public|private|protected)?\\s*interface\\s+(\\w+)",
        Pattern.MULTILINE
    );

    // matches public enum Status
    private static final Pattern ENUM_PATTERN = Pattern.compile(
        "^\\s*(?:public|private|protected)?\\s*enum\\s+(\\w+)",
        Pattern.MULTILINE
    );

    // matches public void doThing(...), private static int calculate(...)
    private static final Pattern METHOD_PATTERN = Pattern.compile(
        "^\\s*(?:public|private|protected)?\\s*(?:static)?\\s*(?:final)?\\s*" +
        "(?:synchronized)?\\s*(?:<[^>]+>\\s+)?(?:\\w+(?:<[^>]*>)?(?:\\[\\])?)\\s+(\\w+)\\s*\\(",
        Pattern.MULTILINE
    );

    // matches import java.util.List
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
        "^\\s*import\\s+(?:static\\s+)?([\\w.]+\\*?);",
        Pattern.MULTILINE
    );

    // matches package com.codeintel.parser
    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
        "^\\s*package\\s+([\\w.]+);",
        Pattern.MULTILINE
    );

    // matches @Override, @Autowired, @Service
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile(
        "^\\s*(@\\w+)",
        Pattern.MULTILINE
    );

    @Override
    public List<ExtractedSymbol> parse(String content) {
        List<ExtractedSymbol> symbols = new ArrayList<>();
        String[] lines = content.split("\n");

        extractWithPattern(content, lines, CLASS_PATTERN, SymbolType.CLASS, symbols);
        extractWithPattern(content, lines, INTERFACE_PATTERN, SymbolType.INTERFACE, symbols);
        extractWithPattern(content, lines, ENUM_PATTERN, SymbolType.ENUM, symbols);
        extractWithPattern(content, lines, METHOD_PATTERN, SymbolType.METHOD, symbols);
        extractWithPattern(content, lines, IMPORT_PATTERN, SymbolType.IMPORT, symbols);
        extractWithPattern(content, lines, PACKAGE_PATTERN, SymbolType.PACKAGE, symbols);
        extractWithPattern(content, lines, ANNOTATION_PATTERN, SymbolType.ANNOTATION, symbols);

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
        return "java";
    }
}