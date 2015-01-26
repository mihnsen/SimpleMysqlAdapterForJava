package com.oe.java.simplemodel;

import com.mysql.jdbc.Connection;
import java.lang.reflect.Field;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author minhnt
 */
public class MySqlAdapter implements Adapter{

	private Connection conn;
    private final String driver = "com.mysql.jdbc.Driver";

    /**
	 * setup mysql connection before use
	 *
	 * @param host		database host to connect
	 * @param port		database port to connect
	 * @param dbName	database name to connect
	 * @param username  database username
	 * @param password  database password
	 */
	@Override
	public void setup(String host, String port, String dbName, String username, String password) {

		try {
			//Load MySQL JDBC Driver
			Class.forName(driver);

			//Open Connection
			String url = "jdbc:mysql://" + host + ":" + port +"/" + dbName;
			conn = (Connection) DriverManager.getConnection(url, username, password);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * prepare statement for query
	 *
	 * @param query		query sql
	 * @return 			parepared statement
	 * @see				PreparedStatement
	 */
	@Override
	public PreparedStatement prepare(String query) {
		try {
			return conn.prepareStatement(query);
		} catch (SQLException ex) {
			Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	/**
	 * load data row to object with id (or primary key).
	 *
	 * @param table		table contain this row
	 * @return true     if find this row and load success, 
     * @return false    if not exits
	 * @see				Table
	 */
	@Override
	public boolean load(Table table) {
		String sql = "SELECT * FROM `"+table.table+"` WHERE `"+table.key+"` = ?";
		java.sql.PreparedStatement p;
		try {
			p = conn.prepareStatement(sql);
	
			Class<?> c = table.getClass();
			Field f = c.getDeclaredField(table.key);
			f.setAccessible(true);
			int id = (int) f.get(table);
			p.setInt(1, id);
			
			ResultSet result = p.executeQuery();
			Field [] attributes =  table.getClass().getDeclaredFields();

			// CALL RESULT 
			result.next();
			int i = 0;
			for (Field field : attributes) {
				field.setAccessible(true);
				String type = field.getType().getSimpleName();
				String fieldName = field.getName();
				
				switch (type) {
					case "int":
						field.set(table, result.getInt(fieldName));
						break;

					case "String":
						field.set(table, result.getString(fieldName));
						break;
						
					default:
						break;
				}
			}

			return result.next();
			
		} catch (SQLException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
		}

		return false;
	}

	/**
	 * Query an SQL query for ex: SELECT * FROM users
	 *
	 * @param obj		    table which we execute the SQL query
	 * @param query		    SQL query which execute
	 * @return ResultSet    result of query
	 * @see				    Table
	 */
	@Override
	public ResultSet query(Object obj, String query) {
		try {
			return execSelect(query, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * select multiple row with select
	 *
	 * @param obj		    table which we execute the SQL query
	 * @return ResultSet    result of query
	 * @see				    Table
	 */
	@Override
	public ResultSet select(Object obj) {
		return this.select(obj, "", "", "", "", 0, 0);
	}
	
	@Override
	public ResultSet select(Object obj, String where) {
		return this.select(obj, where, "", "", "", 0, 0);
	}
	
	@Override
	public ResultSet select(Object obj, String where, String choice) {
		return this.select(obj, where, choice, "", "", 0, 0);
	}
	
	@Override
	public ResultSet select(Object obj, String where, String choice, String order) {
		return this.select(obj, where, choice, order, "", 0, 0);
	}
	
	@Override
	public ResultSet select(Object obj, String where, String choice, String order, String group) {
		return this.select(obj, where, choice, order, group, 0, 0);
	}
	
	@Override
	public ResultSet select(Object obj, String where, String choice, String order, String group, int pageIndex, int pageSize) {
		if(choice == null || choice.isEmpty()) {
			choice = "*";
		}
		
		Table tableObj = (Table) obj;
		String query = "SELECT " + choice + " FROM `" + tableObj.table + "`";
		
		if(where != null && !where.isEmpty()) {
			query+= " WHERE " + where;
		}
		if(group != null && !group.isEmpty()) {
			query+= " GROUP BY " + group;
		}
		if(order != null && !order.isEmpty()) {
			query+= " ORDER BY " + order;
		}
		if(pageIndex >=0 && pageSize > 0) {
			query+= " LIMIT " + (pageIndex*pageSize) + "," + pageSize; 
		}

		try {
			return execSelect(query, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;

	}

	/**
	 * update a row in sql via object attribute
	 *
	 * @param obj		    table which we need update
	 * @param fields		fields need to update
	 * @param where		condition (where)
	 * @return true         if update success
	 * @return false        if update failure
	 * @see				    Table
	 */
	@Override
	public boolean update(Object obj, String fields, String where) {
		Table table = (Table) obj;
		
		String[] fieldArr = fields.split(",", -1);
		String builder = "UPDATE `" + table.getTable() + "` SET ";

		for(int i=0; i<fieldArr.length; i++) {
			String f = fieldArr[i];
			f = f.trim();
			f+= " = ?"+f;
			builder+= f;

			if(i<fieldArr.length-1) {
				builder+= " , ";
			}
		}

		if(where==null || where.trim().equals("")) {
			where = table.getKey() + " = ?" + table.getKey();
		}

		builder+= " WHERE " + where;


		return execUpdate(builder, obj) != -1;
	}

	/**
	 * insert a record to database 
	 *
	 * @param obj		    table which we need insert
	 * @param fields		fields need to insert
	 * @return id           last record object primary key and add primary key value to table object
	 * @see				    Table
	 */
	@Override
	public int insert(Object obj, String fields) {
		Table tableObj = (Table) obj;
		
		String f = " ( ";
		String v = " ( ";

		String[] option = fields.split(",", -1);
		for(int i=0; i<option.length; i++) {
			String value = option[i].trim();

			f+= "`"+value+"`";
			v+= "?"+value;

			if(i < option.length-1) {
				f+= " , ";
				v+= " , ";
			} else {
				f+= " ) ";
				v+= " ) ";
			}
		}
		String query = "INSERT INTO `" + tableObj.getTable() + "`" + f + "VALUE" + v;
		
		return execInsert(query, obj);
	}

	/**
	 * delete a record
	 *
	 * @param obj		    table which we need delete
	 * @param where		    condition to delete
	 * @return true         true if delete success
     * @return false        if delete failure
	 * @see				    Table
	 */
	@Override
	public boolean delete(Object obj, String where) {
        
        Table tableObj = (Table) obj;
		if(where != null && !where.isEmpty()) {
			where = tableObj.key + " = ?" + tableObj.key;
		}
        
        String query = "DELETE FROM `" + tableObj.table + "` WHERE " + where;
		return execUpdate(query, obj) != -1;
	}

	/**
	 * parse query before use
	 *
	 * @param query		    query need parsed
	 * @return true         true if delete success
     * @return string       query using for real sql command
     * @return string[]     list param binding
	 */
	@Override
	public HashMap<String, Object> parseQuery(String query) {
		
		String parsedQuery = "";
		ArrayList<String> params = new ArrayList<String>();

		String[] frag = query.split(" ", -1);

		for(String f : frag) {
			f = f.trim();
			if(f.isEmpty()) {
				continue;
			}

			if(f.charAt(0) == '?') {
				params.add(f.substring(1, f.length()));
				f = "?";
			}
	
			parsedQuery+= f + " ";
		}

		HashMap<String, Object> obj = new HashMap<String, Object>();

		obj.put("query", parsedQuery);
		obj.put("params", params);

		return obj;
	}

	/**
	 * list record with query
	 *
	 * @param query		    query to execute
	 * @param object	    table object which execute
	 * @return ResultSet    result of query
	 * @see				    Table
	 */
	@Override
	public ResultSet execSelect(String query, Object obj) {
		HashMap<String, Object> parsed = parseQuery(query);

		String sql = (String) parsed.get("query");
		PreparedStatement statement = prepare(sql);
		
		ArrayList<String> params = (ArrayList<String>) parsed.get("params");
		Class<?> c = obj.getClass();
	
		for(int i=0; i < params.size(); i++) {
			try {
				Field f = c.getDeclaredField(params.get(i));
				setStatementParam(statement, i, obj, f);
			} catch (SQLException ex) {
				Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
			} catch (NoSuchFieldException ex) {
				Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
			} catch (SecurityException ex) {
				Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IllegalArgumentException ex) {
				Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IllegalAccessException ex) {
				Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
			}
		}


		ResultSet result = null;
		try {
			result = statement.executeQuery(sql);
		} catch (SQLException ex) {
			Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		return result;
	}
	
	/**
	 * binding param to statement
	 *
	 * @param s		        statement which need binding
	 * @param idx           position to binding
	 * @param object	    table object which execute query
	 * @param f	            field attribute of table to biding
	 * @see				    Table
	 */
	private void setStatementParam(PreparedStatement s, int idx, Object o, Field f) throws IllegalArgumentException, IllegalAccessException, SQLException {
	
		f.setAccessible(true);

		String type = f.getType().getSimpleName();
		switch(type) {
			case "int": 
				int valInt = (int) f.get(o);
				s.setInt(idx, valInt);
				break;
			case "String":
				String valStr = (String) f.get(o);
				s.setString(idx, valStr);
				break;

			default:
				break;
		}	
	}

    /**
	 * update a record with query
	 *
	 * @param query		    query to execute
	 * @param object	    table object which execute
	 * @see				    Table
	 */
	@Override
	public int execUpdate(String query, Object obj) {
		HashMap<String, Object> parsed = parseQuery(query);

		String sql = (String) parsed.get("query");
		PreparedStatement statement = prepare(sql);

		ArrayList<String> params = (ArrayList<String>) parsed.get("params");
		Class<?> c = obj.getClass();
	
		for(int i=0; i < params.size(); i++) {
			try {
				Field f = c.getDeclaredField(params.get(i));
				setStatementParam(statement, i+1, obj, f);
				
			} catch (SQLException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
				Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		ResultSet result = null;
		try {
			return statement.executeUpdate();
		} catch (SQLException ex) {
			Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		return -1;
	}

	/**
	 * insert a record with query
	 *
	 * @param query		    query to execute
	 * @param object	    table object which execute
	 * @return int          id of row inserted
	 * @see				    Table
	 */
	@Override
	public int execInsert(String query, Object obj) {
		HashMap<String, Object> parsed = parseQuery(query);

		String sql = (String) parsed.get("query");
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException ex) {
			Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		ArrayList<String> params = (ArrayList<String>) parsed.get("params");
		Class<?> c = obj.getClass();
	
		for(int i=0; i < params.size(); i++) {
			try {
				Field f = c.getDeclaredField(params.get(i));
				setStatementParam(statement, i+1, obj, f);
			} catch (SQLException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
				Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		try {
			int affectedRows = statement.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException("Creating user failed, no rows affected.");
			}

			ResultSet generatedKeys = statement.getGeneratedKeys();
			if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
				
				Table table = (Table) obj;

				Field field = table.getClass().getDeclaredField(table.getKey());
				field.setAccessible(true);
				field.set(table, id);

				return id;
            }
            else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
		} catch (SQLException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			Logger.getLogger(MySqlAdapter.class.getName()).log(Level.SEVERE, null, ex);
		}
		return -1;
	}

}
