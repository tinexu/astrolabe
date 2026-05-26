package main.java.com.codeintel.service;

import com.codeintel.model.ExtractedSymbol;
import com.codeintel.model.FileJob;
import com.codeintel.model.ParsedFile;
import com.codeintel.parser.LanguageParser;
import com.codeintel.parser.ParserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Service
public class FileProcessingService {

    private static final Logger log = LoggerFactory.getLogger(FileProcessingService.class);
    private static final long MAX_FILE_SIZE = 1_000_000; // skip huge generated files

    private final ParserRegistry parserRegistry;

    public FileProcessingService(ParserRegistry parserRegistry) {
        this.parserRegistry = parserRegistry;
    }

    public ParsedFile process(FileJob job, Path repoRoot) {
        ParsedFile parsed = new ParsedFile();
        parsed.setRepoId(job.getRepoId());
        parsed.setRepoName(job.getRepoName());
        parsed.setFilePath(job.getFilePath());
        parsed.setLanguage(job.getLanguage());
        parsed.setFileSizeBytes(job.getFileSizeBytes());

        // guard to skip oversized files
        if (job.getFileSizeBytes() > MAX_FILE_SIZE) {
            log.warn("Skipping oversized file: {} ({} bytes)", job.getFilePath(), job.getFileSizeBytes());
            parsed.setSymbols(Collections.emptyList());
            return parsed;
        }

        try {
            // read the file content
            Path filePath = repoRoot.resolve(job.getFilePath());
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            parsed.setRawContent(content);
            parsed.setTotalLines(content.split("\n").length);

            // route to the correct parser
            List<ExtractedSymbol> symbols = parserRegistry.getParser(job.getLanguage())
                .map(parser -> parser.parse(content))
                .orElseGet(() -> {
                    log.debug("No parser for language: {}", job.getLanguage());
                    return Collections.emptyList();
                });

            parsed.setSymbols(symbols);

            log.info("Parsed {} — {} symbols ({} lines)",
                job.getFilePath(), symbols.size(), parsed.getTotalLines());

        } catch (IOException e) {
            log.error("Failed to read file {}: {}", job.getFilePath(), e.getMessage());
            parsed.setSymbols(Collections.emptyList());
        } catch (Exception e) {
            log.error("Parsing failed for {}: {}", job.getFilePath(), e.getMessage());
            parsed.setSymbols(Collections.emptyList());
        }

        return parsed;
    }
}