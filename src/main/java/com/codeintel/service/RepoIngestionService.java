package main.java.com.codeintel.service;

import com.codeintel.model.FileJob;
import com.codeintel.model.RepoMetadata;
import com.codeintel.repository.RepoMetadataRepository;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RepoIngestionService {

    private static final Logger log = LoggerFactory.getLogger(RepoIngestionService.class);

    private final RepoMetadataRepository repoMetadataRepository;

    // directories and files to skip during tree walk
    private static final Set<String> IGNORED_DIRS = Set.of(
        ".git", "node_modules", ".idea", ".vscode", "target",
        "build", "dist", "__pycache__", ".gradle", "vendor"
    );

    // extensions we consider "source code"
    private static final Map<String, String> EXTENSION_TO_LANGUAGE = Map.ofEntries(
        Map.entry(".java", "java"),
        Map.entry(".py", "python"),
        Map.entry(".js", "javascript"),
        Map.entry(".ts", "typescript"),
        Map.entry(".tsx", "typescript"),
        Map.entry(".jsx", "javascript"),
        Map.entry(".go", "go"),
        Map.entry(".rs", "rust"),
        Map.entry(".cpp", "cpp"),
        Map.entry(".c", "c"),
        Map.entry(".h", "c"),
        Map.entry(".rb", "ruby"),
        Map.entry(".scala", "scala"),
        Map.entry(".kt", "kotlin"),
        Map.entry(".swift", "swift"),
        Map.entry(".cs", "csharp"),
        Map.entry(".php", "php"),
        Map.entry(".sql", "sql"),
        Map.entry(".xml", "xml"),
        Map.entry(".yaml", "yaml"),
        Map.entry(".yml", "yaml"),
        Map.entry(".json", "json"),
        Map.entry(".md", "markdown"),
        Map.entry(".sh", "shell"),
        Map.entry(".dockerfile", "docker")
    );

    public RepoIngestionService(RepoMetadataRepository repoMetadataRepository) {
        this.repoMetadataRepository = repoMetadataRepository;
    }

    public RepoMetadata ingest(String repoUrl, String branch) throws Exception {
        // 1. extract repo name from URL
        String repoName = extractRepoName(repoUrl);
        String effectiveBranch = (branch != null && !branch.isBlank()) ? branch : "main";

        // 2. create metadata record with IN_PROGRESS status
        RepoMetadata metadata = new RepoMetadata();
        metadata.setRepoUrl(repoUrl);
        metadata.setRepoName(repoName);
        metadata.setBranch(effectiveBranch);
        repoMetadataRepository.save(metadata);

        Path tempDir = null;
        try {
            // 3. clone repo to temp directory
            tempDir = Files.createTempDirectory("codeintel-" + repoName);
            log.info("Cloning {} (branch: {}) into {}", repoUrl, effectiveBranch, tempDir);

            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(tempDir.toFile())
                .setBranch(effectiveBranch)
                .setDepth(1)  // shallow clone — we only need current file state
                .call()
                .close();

            // 4. walk file tree and build job list
            List<FileJob> jobs = walkAndCollectJobs(tempDir, metadata.getRepoId(), repoName);

            // 5. update metadata with final count
            metadata.setTotalFiles(jobs.size());
            metadata.setStatus("COMPLETED");
            metadata.setLastUpdated(Instant.now());
            repoMetadataRepository.save(metadata);

            log.info("Ingestion complete: {} files from {}", jobs.size(), repoName);

            // In step 2, this is where we'd publish jobs to RabbitMQ.
            // For now, jobs are built but not dispatched.
            // publishToQueue(jobs);  <-- wired in step 2

            return metadata;

        } catch (Exception e) {
            metadata.setStatus("FAILED");
            metadata.setLastUpdated(Instant.now());
            repoMetadataRepository.save(metadata);
            log.error("Ingestion failed for {}: {}", repoUrl, e.getMessage());
            throw e;
        } finally {
            // 6. clean up cloned repo
            if (tempDir != null) {
                deleteDirectory(tempDir);
            }
        }
    }

    private List<FileJob> walkAndCollectJobs(Path repoRoot, UUID repoId, String repoName)
            throws IOException {
        List<FileJob> jobs = new ArrayList<>();

        Files.walkFileTree(repoRoot, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String dirName = dir.getFileName().toString();
                if (IGNORED_DIRS.contains(dirName)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String fileName = file.getFileName().toString();
                String extension = getExtension(fileName);
                String language = EXTENSION_TO_LANGUAGE.get(extension);

                if (language != null) {
                    FileJob job = new FileJob();
                    job.setRepoId(repoId);
                    job.setRepoName(repoName);
                    job.setFilePath(repoRoot.relativize(file).toString());
                    job.setLanguage(language);
                    job.setFileSizeBytes(attrs.size());
                    jobs.add(job);
                }

                return FileVisitResult.CONTINUE;
            }
        });

        return jobs;
    }

    private String extractRepoName(String url) {
        // "https://github.com/user/repo-name.git" -> "user/repo-name"
        String cleaned = url.replaceAll("\\.git$", "");
        String[] parts = cleaned.split("/");
        if (parts.length >= 2) {
            return parts[parts.length - 2] + "/" + parts[parts.length - 1];
        }
        return cleaned;
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot >= 0) ? fileName.substring(dot).toLowerCase() : "";
    }

    private void deleteDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException exc)
                    throws IOException {
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
