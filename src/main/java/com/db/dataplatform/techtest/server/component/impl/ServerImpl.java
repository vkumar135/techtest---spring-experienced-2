package com.db.dataplatform.techtest.server.component.impl;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.service.DataHeaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerImpl implements Server {

    private final DataBodyService dataBodyServiceImpl;
    private final DataHeaderService dataHeaderServiceImpl;
    private final ModelMapper modelMapper;

    /**
     * @param envelope
     * @return true if there is a match with the client provided checksum.
     */
    @Override
    public boolean saveDataEnvelope(DataEnvelope envelope) {

        // Save to persistence.
        persist(envelope);

        log.info("Data persisted successfully, data name: {}", envelope.getDataHeader().getName());
        return true;
    }

    @Override
    public List<DataBodyEntity> getAllBlockTypes(BlockTypeEnum blockType) {
        return dataBodyServiceImpl.getDataByBlockType(blockType);
    }

    @Override
    public boolean updateByBlockName(String blockName, String newBlockType) {
        DataHeaderEntity dataHeaderEntity = dataHeaderServiceImpl.getDataByBlockName(blockName).get();
        dataHeaderEntity.setBlocktype(BlockTypeEnum.getBlockTypeEnumByTypeName(newBlockType).get());
        dataHeaderEntity = persist(dataHeaderEntity);

        if (dataHeaderEntity.getDataHeaderId() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private DataHeaderEntity persist(DataHeaderEntity dataHeaderEntity) {
        log.info("Persisting data with attribute name: {}", dataHeaderEntity.getName());
        return saveData(dataHeaderEntity);
    }

    private void persist(DataEnvelope envelope) {
        log.info("Persisting data with attribute name: {}", envelope.getDataHeader().getName());
        DataHeaderEntity dataHeaderEntity = modelMapper.map(envelope.getDataHeader(), DataHeaderEntity.class);

        DataBodyEntity dataBodyEntity = modelMapper.map(envelope.getDataBody(), DataBodyEntity.class);
        dataBodyEntity.setDataHeaderEntity(dataHeaderEntity);

        saveData(dataBodyEntity);
    }

    private void saveData(DataBodyEntity dataBodyEntity) {
        dataBodyServiceImpl.saveDataBody(dataBodyEntity);
    }

    private DataHeaderEntity saveData(DataHeaderEntity dataHeaderEntity) {
        return dataHeaderServiceImpl.saveHeader(dataHeaderEntity);
    }

}
