package main.java.com.codeintel.model;

public class IngestionRequest {
    private String repoUrl;
    private String branch; // optional but defaults to "main"

    public IngestionRequest() {}

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
}
