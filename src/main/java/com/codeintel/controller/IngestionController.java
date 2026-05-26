package main.java.com.codeintel.controller;

import com.codeintel.model.IngestionRequest;
import com.codeintel.model.RepoMetadata;
import com.codeintel.service.RepoIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/repos")
public class IngestionController {

    private final RepoIngestionService ingestionService;

    public IngestionController(RepoIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<?> ingestRepo(@RequestBody IngestionRequest request) {
        try {
            RepoMetadata result = ingestionService.ingest(
                request.getRepoUrl(),
                request.getBranch()
            );
            return ResponseEntity.ok(Map.of(
                "repoId", result.getRepoId(),
                "repoName", result.getRepoName(),
                "totalFiles", result.getTotalFiles(),
                "status", result.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Ingestion failed",
                "message", e.getMessage()
            ));
        }
    }
}
