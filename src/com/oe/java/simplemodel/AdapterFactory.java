package com.oe.java.simplemodel;


/**
 *
 * @author minhnt
 */
public class AdapterFactory {

 	private static MySqlAdapter adapter;       
    private static AdapterFactory instance = null;
    private AdapterFactory() {}
 
    public static synchronized AdapterFactory getInstance() {
        if (instance == null) {
            instance = new AdapterFactory();
        }
 
        return instance;
    }
    
	public static MySqlAdapter setupAdapter(String host, String port, String dbName, String username, String password) {
		if(adapter == null) {
			adapter = new MySqlAdapter();
			adapter.setup(host, port, dbName, username, password);
		}
		return adapter;
	}

	public static MySqlAdapter getAdapter() {
		return adapter;
	}
}
