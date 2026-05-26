package main.java.com.codeintel.model;

import java.util.UUID;

public class FileJob {
    private UUID jobId;
    private UUID repoId;
    private String repoName;
    private String filePath;     // relative path within the repo
    private String language;     // detected from extension
    private long fileSizeBytes;

    public FileJob() {
        this.jobId = UUID.randomUUID();
    }

    // getters and setters
    public UUID getJobId() { return jobId; }
    public UUID getRepoId() { return repoId; }
    public void setRepoId(UUID repoId) { this.repoId = repoId; }
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
}
