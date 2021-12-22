package com.fraunhofer.de.datamongo.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Schema extends Mapping {
    private String columns;

    @Builder(builderMethodName = "SchemaBuilder")
    private Schema(final String _id,
                   final String entity,
                   final String type,
                   final String source,
                   final Option options,
                   final List<Setting> settingList,
                   final String columns) {
        super(_id, entity, type, source, options, settingList);
        this.columns = columns;
    }
}
