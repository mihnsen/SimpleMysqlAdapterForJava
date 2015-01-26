# Simple Mysql Adapter For Java                                                                                     

This is simple mysql adapter for Java. Easy to working with JDBC MySQL.

# Installation
1. Method 1 : Include source code to your package
2. Method 2 : Adding jar file to your library

# How to use
Setup Adapter before use:
    
    AdapterFactory.getInstance().setupAdapter("mysql_host", "mysql_port", "mysql_database", "mysql_username", "mysql_password");

All model have to extends from Table, and you have to set primary key and table name:

    public class Product extends Table {
    
    	public int id;
    	public String name;
    	public String description;
    	
    	public Product() {
    		setKey("id");
    		setTable("product");
    	}
    }

# Initial Model
    Product product = new Product();
    
# QUERY a SQL
    ResultSet result = product.query("SELECT * FROM product");
    
# SELECT
Return result set of model
    ResultSet result = product.select("id = 1");
    
# LOAD model object using primary key
Return model with all attribute data
    product.id = 1;
    product.load();

# INSERT
Return last inserted id
    product.name = "OE Proj";
    product.description = "Made by Ownego";
    product.insert("namem, description");
    //add set product.id to inserted id
# UPDATE
Return true or false
    product.name = "OE Proj";
    product.description = "Made by Ownego";
    product.update("namem, description");
    
# DELETE
Return true or false
    product.delete("name = "+"unknow");
    
License
----

Apache License
