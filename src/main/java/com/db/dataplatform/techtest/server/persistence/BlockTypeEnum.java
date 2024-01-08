package com.db.dataplatform.techtest.server.persistence;

import java.util.Arrays;
import java.util.Optional;

public enum BlockTypeEnum
{
    BLOCKTYPEA("blocktypea"),
    BLOCKTYPEB("blocktypeb");

    private final String type;

    BlockTypeEnum(String type)
    {
        this.type = type;
    }

    // Reverse lookup methods
    public static Optional<BlockTypeEnum> getBlockTypeEnumByTypeName(String value)
    {
        return Optional.ofNullable(
                Arrays.stream(BlockTypeEnum.values())
                        .filter(blockType -> blockType.type.equalsIgnoreCase(value))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Block Type provided not exist!")));
    }

}
