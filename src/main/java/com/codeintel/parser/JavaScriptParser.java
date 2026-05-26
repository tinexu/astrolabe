package main.java.com.codeintel.parser;

import com.codeintel.model.ExtractedSymbol;
import com.codeintel.model.SymbolType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaScriptParser implements LanguageParser {

    // matches class App, class App extends Component
    private static final Pattern CLASS_PATTERN = Pattern.compile(
        "^\\s*(?:export\\s+)?(?:default\\s+)?class\\s+(\\w+)",
        Pattern.MULTILINE
    );

    // matches function doThing(...), async function doThing(...)
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
        "^\\s*(?:export\\s+)?(?:async\\s+)?function\\s+(\\w+)\\s*\\(",
        Pattern.MULTILINE
    );

    // matches const doThing = (...), const doThing = function(...)
    private static final Pattern ARROW_FUNCTION_PATTERN = Pattern.compile(
        "^\\s*(?:export\\s+)?(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?:async\\s+)?(?:\\([^)]*\\)|\\w+)\\s*=>",
        Pattern.MULTILINE
    );

    // matches import { x } from 'y', import x from 'y'
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
        "^\\s*import\\s+(?:\\{[^}]*\\}|\\w+)(?:\\s*,\\s*(?:\\{[^}]*\\}|\\w+))?\\s+from\\s+['\"]([^'\"]+)['\"]",
        Pattern.MULTILINE
    );

    // matches const/let/var require('...')
    private static final Pattern REQUIRE_PATTERN = Pattern.compile(
        "^\\s*(?:const|let|var)\\s+(\\w+)\\s*=\\s*require\\(['\"]([^'\"]+)['\"]\\)",
        Pattern.MULTILINE
    );

    // matches method(args) { inside a class body
    private static final Pattern METHOD_PATTERN = Pattern.compile(
        "^\\s+(?:async\\s+)?(?:static\\s+)?(?:get\\s+|set\\s+)?(\\w+)\\s*\\([^)]*\\)\\s*\\{",
        Pattern.MULTILINE
    );

    @Override
    public List<ExtractedSymbol> parse(String content) {
        List<ExtractedSymbol> symbols = new ArrayList<>();
        String[] lines = content.split("\n");

        extractWithPattern(content, lines, CLASS_PATTERN, SymbolType.CLASS, symbols);
        extractWithPattern(content, lines, FUNCTION_PATTERN, SymbolType.FUNCTION, symbols);
        extractWithPattern(content, lines, ARROW_FUNCTION_PATTERN, SymbolType.FUNCTION, symbols);
        extractWithPattern(content, lines, METHOD_PATTERN, SymbolType.METHOD, symbols);

        // ES module imports
        Matcher importMatcher = IMPORT_PATTERN.matcher(content);
        while (importMatcher.find()) {
            String module = importMatcher.group(1);
            int lineNumber = getLineNumber(content, importMatcher.start());
            symbols.add(new ExtractedSymbol(
                module, SymbolType.IMPORT, lineNumber, lines[lineNumber - 1].trim()
            ));
        }

        // CommonJS requires
        Matcher requireMatcher = REQUIRE_PATTERN.matcher(content);
        while (requireMatcher.find()) {
            String module = requireMatcher.group(2);
            int lineNumber = getLineNumber(content, requireMatcher.start());
            symbols.add(new ExtractedSymbol(
                module, SymbolType.IMPORT, lineNumber, lines[lineNumber - 1].trim()
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
        return "javascript";
    }
}