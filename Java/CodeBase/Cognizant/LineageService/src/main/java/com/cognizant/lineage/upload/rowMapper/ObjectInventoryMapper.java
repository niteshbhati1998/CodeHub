package com.cognizant.lineage.upload.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.cognizant.lineage.upload.model.ObjectInventory;

public class ObjectInventoryMapper implements RowMapper<ObjectInventory> {
    @Override
    public ObjectInventory mapRow(ResultSet rs, int rowNum) throws SQLException {
        ObjectInventory inventory = new ObjectInventory();
        inventory.setNodeType(rs.getString("node_type"));
        inventory.setCountOfObjects(rs.getInt("count"));
        return inventory;
    }
}
