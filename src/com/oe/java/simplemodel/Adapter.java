package com.oe.java.simplemodel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 *
 * @author minhnt
 */
public interface Adapter {
    
    public abstract void setup(String host, String port, String dbName, String username, String password);;;
    
    public abstract PreparedStatement prepare(String query);
    public abstract boolean load(Table table);
    public abstract ResultSet query(Object obj, String query);
	public abstract ResultSet select(Object obj);
	public abstract ResultSet select(Object obj, String where);
	public abstract ResultSet select(Object obj, String where, String choice);
	public abstract ResultSet select(Object obj, String where, String choice, String order);
	public abstract ResultSet select(Object obj, String where, String choice, String order, String group);
    public abstract ResultSet select(Object obj, String where, String choice, String order, String group, int pageIndex, int pageSize);
    public abstract boolean update(Object obj, String field, String where);
    public abstract int insert(Object obj, String fields);
    public abstract boolean delete(Object obj, String where);
    
    public abstract HashMap<String, Object> parseQuery(String query);
    public abstract ResultSet execSelect(String query, Object obj);
    public abstract int execUpdate(String query, Object obj);
    public abstract int execInsert(String query, Object obj);
}
