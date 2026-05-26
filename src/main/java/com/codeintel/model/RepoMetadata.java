package main.java.com.codeintel.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Table("repo_metadata")
public class RepoMetadata {
    @PrimaryKey
    private UUID repoId;
    private String repoUrl;
    private String repoName;
    private String branch;
    private int totalFiles;
    private String status; // IN_PROGRESS, COMPLETED, FAILED
    private Instant ingestedAt;
    private Instant lastUpdated;

    public RepoMetadata() {
        this.repoId = UUID.randomUUID();
        this.ingestedAt = Instant.now();
        this.lastUpdated = Instant.now();
        this.status = "IN_PROGRESS";
    }

    // getters and setters
    public UUID getRepoId() { return repoId; }
    public void setRepoId(UUID repoId) { this.repoId = repoId; }
    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public int getTotalFiles() { return totalFiles; }
    public void setTotalFiles(int totalFiles) { this.totalFiles = totalFiles; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getIngestedAt() { return ingestedAt; }
    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}