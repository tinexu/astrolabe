package main.java.com.codeintel.repository;

import com.codeintel.model.RepoMetadata;
import org.springframework.data.cassandra.repository.CassandraRepository;
import java.util.UUID;

public interface RepoMetadataRepository extends CassandraRepository<RepoMetadata, UUID> {
}

