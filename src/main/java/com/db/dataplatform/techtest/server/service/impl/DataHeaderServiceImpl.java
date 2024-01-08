package com.db.dataplatform.techtest.server.service.impl;

import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.persistence.repository.DataHeaderRepository;
import com.db.dataplatform.techtest.server.service.DataHeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DataHeaderServiceImpl implements DataHeaderService
{

    private final DataHeaderRepository dataHeaderRepository;

    @Override
    public DataHeaderEntity saveHeader(DataHeaderEntity entity) {
        return dataHeaderRepository.save(entity);
    }

    @Override
    public Optional<DataHeaderEntity> getDataByBlockName(String blockName) {
        return dataHeaderRepository.findByName(blockName);
    }
}
