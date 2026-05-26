package main.java.com.codeintel.parser;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ParserRegistry {

    private final Map<String, LanguageParser> parsers = new HashMap<>();

    // auto-injects all LanguageParser implementations
    public ParserRegistry(List<LanguageParser> parserList) {
        for (LanguageParser parser : parserList) {
            parsers.put(parser.getLanguage(), parser);
        }

        // alias mappings
        parsers.putIfAbsent("typescript", parsers.get("javascript"));
    }

    public Optional<LanguageParser> getParser(String language) {
        return Optional.ofNullable(parsers.get(language));
    }

    public boolean supports(String language) {
        return parsers.containsKey(language);
    }
}