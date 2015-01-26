package com.oe.java.simplemodel;

import java.sql.ResultSet;

/**
 *
 * @author minhnt
 */
public class Table {
	
	public String key;
	public String table;

	protected MySqlAdapter adapter;
	
	public Table () {
		setupAdapter();
	}

	private void setupAdapter() {
		adapter = AdapterFactory.getInstance().getAdapter();
	}

	public String getKey() {
		return this.key;
	}

	public String getTable() {
		return this.table;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String load() {
		adapter.load(this);
		return null;
	}

	public ResultSet query(String query) {
		return adapter.query(this, query);
	}

	public ResultSet select() {
		return adapter.select(this, "", "", "", "", 0, 0);
	}

	public ResultSet select(String where) {
		return adapter.select(this, where, "", "", "", 0, 0);
	}

	public ResultSet select(String where, String choice) {
		return adapter.select(this, where, choice, "", "", 0, 0);
	}
	
	public ResultSet select(String where, String choice, String order) {
		return adapter.select(this, where, choice, order, "", 0, 0);
	}
	
	public ResultSet select(String where, String choice, String order, String group) {
		return adapter.select(this, where, choice, order, group, 0, 0);
	}
	
	public ResultSet select(String where, String choice, String order, String group, int pageIndex, int pageSize) {
		return adapter.select(this, where, choice, order, group, pageIndex, pageSize);
	}
	
	public Boolean update(String fields, String where) {
		return adapter.update(this, fields, where);
	}

	public int insert(String fields) {
		return adapter.insert(this, fields);
	}

	public Boolean delete(String where) {
		return adapter.delete(this, where);
	}
	
}
