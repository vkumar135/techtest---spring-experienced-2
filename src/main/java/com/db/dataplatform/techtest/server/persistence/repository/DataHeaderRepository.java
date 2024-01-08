package com.db.dataplatform.techtest.server.persistence.repository;

import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataHeaderRepository extends JpaRepository<DataHeaderEntity, Long>
{
    Optional<DataHeaderEntity> findByName(String name);
}
