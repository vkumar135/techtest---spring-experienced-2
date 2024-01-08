package com.db.dataplatform.techtest.server.persistence.repository;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataStoreRepository extends JpaRepository<DataBodyEntity, Long> {
    @Query(
            "select store from DataBodyEntity store, DataHeaderEntity header \n"
                    + "where store.dataHeaderEntity.dataHeaderId = header.dataHeaderId \n"
                    + "and header.blocktype = :blockType ")
    List<DataBodyEntity> getDataByBlockType(@Param("blockType") BlockTypeEnum blockType);

    @Query(
            "select store from DataBodyEntity store, DataHeaderEntity header \n"
                    + "where store.dataHeaderEntity.dataHeaderId = header.dataHeaderId \n"
                    + "and header.name = :name ")
    List<DataBodyEntity> getDataByBlockName(@Param("name") String name);
}
