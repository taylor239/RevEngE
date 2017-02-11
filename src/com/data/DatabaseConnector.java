package com.data;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.servlet.http.Part;



/**
 * 
 * @author Mi Laptop
 *
 */
public class DatabaseConnector
{
	//private String userName, password, address;
	//private String baseAddr="localhost";
	/**
	 * This is the driver to be used.  It is default a mySql driver, but can be set later.
	 */
	//private String driver="com.mysql.jdbc.Driver";
	/**
	 * 
	 */
	//private int maxConnections=100;
	private boolean verbose = true;
	/**
	 * This is my connection.  It connects to the desired database.
	 */
	private Connection connection=null;
	/**
	 * 
	 */
	private DatabaseConnectionPool myPool=null;
	/**
	 * 
	 */
	private String databaseName="";
	/**
	 * 
	 */
	public DatabaseConnector(String name)
	{
		databaseName=name;
		//address="jdbc:mysql://"+baseAddr+":3306/taylorwiley";
		//userName="taylorwiley"; password="4UB89VGDYyAGwebG";
		//System.err.println(tmp);
		//myPool=DatabaseConnectionPool.getInstance(address, driver, userName, maxConnections, password);
	}
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public synchronized String connect() throws Exception
	{
		String myReturn="";
		//connection=myPool.getConnection();
		/*
		try
		{
			Class.forName(driver);
			connection=DriverManager.getConnection(address, userName, password);
		}
		catch(Exception e)
		{
			myReturn=e.toString();
			throw e;
		}
		*/
		return myReturn;
	}
	public synchronized String getConnection()
	{
		if(verbose)
		{
			System.out.println("Getting a connection");
		}
		String myReturn="";
		if(connection!=null)
		{
			myReturn="already have connection";
			return myReturn;
		}
		ConcurrentHashMap tmp=DatabaseInformationManager.getInstance().getNext(databaseName);
		if(verbose)
		{
			System.out.println("Got a connection pool");
		}
		//System.err.println(tmp);
		if(tmp==null)
		{
			myReturn="no databases found";
			return myReturn;
		}
		myPool=DatabaseConnectionPool.getInstance((String)tmp.get("address"), (String)tmp.get("driver"), (String)tmp.get("username"), (Integer)tmp.get("maxconnections"), (String)tmp.get("password"));
		while(connection==null)
		{
			//System.out.println("Getting connection");
			connection=myPool.getConnection();
		}
		/*
		try
		{
			Class.forName(driver);
			connection=DriverManager.getConnection(address, userName, password);
		}
		catch(Exception e)
		{
			myReturn=e.toString();
			throw e;
		}
		*/
		return myReturn;
	}
	public synchronized void disconnect()
	{
		if(connection==null)
		{
			return;
		}
		myPool.returnConnection(connection);
		connection=null;
	}
	/**
	 * 
	 * @return
	 */
	public synchronized int getNumConnections()
	{
		return myPool.getNumConnections();
	}
	/**
	 * 
	 * @return
	 */
	public synchronized int getNumConnectionsInPool()
	{
		return myPool.getNumConnectionsInPool();
	}
	/**
	 * 
	 * @return
	 */
	public synchronized int getNumConnectionsCheckedOut()
	{
		return myPool.getNumConnectionsCheckedOut();
	}
	/**
	 * 
	 * @return
	 */
	public synchronized String getErrors()
	{
		return myPool.errors;
	}
	/**
	 * 
	 */
	public synchronized void finalize()
	{
		connection=null;
		/*
		try
		{
			connection.close();
		}
		catch(SQLException e1)
		{
			e1.printStackTrace();
		}
		*/
		try
		{
			super.finalize();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param table
	 * @return
	 */
	public synchronized ArrayList getColumnNames(String table)
	{
		ArrayList myReturn=new ArrayList();
		getConnection();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE  `TABLE_SCHEMA` = '"+databaseName+"' AND  `TABLE_NAME` =  ?");
			myStmt.setString(1, table);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			while(myResults.next())
			{
				myReturn.add(myResults.getObject(1));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			disconnect();
			return null;
		}
		disconnect();
		return myReturn;
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public synchronized User signIn(String username, String password, String ipAddress)
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM user INNER JOIN role ON user.email = role.email WHERE user.email = ? AND password = SHA2((SELECT CONCAT(?, (SELECT salt FROM user WHERE email = ?))), 512)");
			myStmt.setString(1, username);
			myStmt.setString(2, password);
			myStmt.setString(3, username);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			if(myResults.next())
			{
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
			}
			else
			{
				disconnect();
				return null;
			}
		}
		catch(Exception e)
		{
			disconnect();
			return null;
		}
		User myReturn=new User(attributes, false, tables);
		disconnect();
		return myReturn;
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public synchronized User getUser(String username)
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM user INNER JOIN role ON user.email = role.email WHERE user.email = ?");
			myStmt.setString(1, username);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			if(myResults.next())
			{
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
			}
			else
			{
				disconnect();
				return null;
			}
		}
		catch(Exception e)
		{
			disconnect();
			return null;
		}
		User myReturn=new User(attributes, false, tables);
		disconnect();
		return myReturn;
	}
	
	/**
	 * 
	 * @param username
	 * @return
	 */
	public synchronized ArrayList getAdminChallenges(String username)
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		ArrayList myReturn = new ArrayList();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM challenge WHERE challenge.admin_email = ?");
			myStmt.setString(1, username);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			while(myResults.next())
			{
				attributes=new ConcurrentHashMap();
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
				DBObj tmp = new DBObj(attributes, false, tables);
				myReturn.add(tmp);
			}
		}
		catch(Exception e)
		{
			disconnect();
			return null;
		}
		disconnect();
		return myReturn;
	}
	
	/**
	 * 
	 * @param username
	 * @return
	 */
	public synchronized ArrayList getAdminStudents(String username)
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		ArrayList myReturn = new ArrayList();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM user INNER JOIN role ON user.email = role.email WHERE role.administrator = ? ORDER BY role.course");
			myStmt.setString(1, username);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			while(myResults.next())
			{
				attributes=new ConcurrentHashMap();
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
				DBObj tmp = new DBObj(attributes, false, tables);
				myReturn.add(tmp);
			}
		}
		catch(Exception e)
		{
			disconnect();
			return null;
		}
		disconnect();
		return myReturn;
	}
	
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public synchronized ArrayList getChallenges(String username)
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		ArrayList myReturn = new ArrayList();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM challenge_participant INNER JOIN challenge ON challenge_participant.challenge_name = challenge.challenge_name WHERE challenge_participant.email = ?");
			myStmt.setString(1, username);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			while(myResults.next())
			{
				attributes=new ConcurrentHashMap();
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
				DBObj tmp = new DBObj(attributes, false, tables);
				myReturn.add(tmp);
			}
		}
		catch(Exception e)
		{
			disconnect();
			return null;
		}
		disconnect();
		return myReturn;
	}
	
	/**
	 * 
	 * @param username
	 * @return
	 */
	public synchronized ArrayList getChallengeAssignment(String username, String challengeName)
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		ArrayList myReturn = new ArrayList();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM `challenge_participant` INNER JOIN `challenge` ON `challenge`.`challenge_name` = `challenge_participant`.`challenge_name` INNER JOIN `role` ON `challenge_participant`.`email` = `role`.`email` WHERE `challenge`.`admin_email` = ? AND `challenge`.`challenge_name` = ? ORDER BY `role`.`course`");
			myStmt.setString(1, username);
			myStmt.setString(2, challengeName);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			while(myResults.next())
			{
				attributes=new ConcurrentHashMap();
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
				DBObj tmp = new DBObj(attributes, false, tables);
				myReturn.add(tmp);
			}
		}
		catch(Exception e)
		{
			disconnect();
			return null;
		}
		disconnect();
		return myReturn;
	}
	
	public synchronized ArrayList getChallenge(String challengeName, String email)
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		ArrayList myReturn = new ArrayList();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM challenge INNER JOIN challenge_command ON challenge.challenge_name = challenge_command.challenge_name INNER JOIN challenge_participant ON challenge.challenge_name = challenge_participant.challenge_name WHERE challenge.challenge_name = ? AND challenge_participant.email = ? ORDER BY challenge_command.command_order ASC");
			myStmt.setString(1, challengeName);
			myStmt.setString(2, email);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			while(myResults.next())
			{
				attributes=new ConcurrentHashMap();
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
				DBObj tmp = new DBObj(attributes, false, tables);
				myReturn.add(tmp);
			}
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
			return null;
		}
		disconnect();
		return myReturn;
	}
	
	public synchronized ArrayList getChallengeSubmission(String challengeName, String email)
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		ArrayList myReturn = new ArrayList();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM `challenge_participant` WHERE `challenge_name`=? AND `email`=?");
			myStmt.setString(1, challengeName);
			myStmt.setString(2, email);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			while(myResults.next())
			{
				attributes=new ConcurrentHashMap();
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
				DBObj tmp = new DBObj(attributes, false, tables);
				myReturn.add(tmp);
			}
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
			return null;
		}
		disconnect();
		return myReturn;
	}
	
	public synchronized ArrayList getChallengeSubmissions(String challengeName)
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		ArrayList myReturn = new ArrayList();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM `challenge_participant` WHERE `challenge_name`=?");
			myStmt.setString(1, challengeName);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			while(myResults.next())
			{
				attributes=new ConcurrentHashMap();
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
				DBObj tmp = new DBObj(attributes, false, tables);
				myReturn.add(tmp);
			}
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
			return null;
		}
		disconnect();
		return myReturn;
	}
	
	public synchronized ArrayList getChallengeDefaults()
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		ArrayList myReturn = new ArrayList();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM challenge_default INNER JOIN challenge_command_default ON challenge_default.challenge_name = challenge_command_default.challenge_name ORDER BY challenge_command_default.command_order ASC");
			//myStmt.setString(1, username);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			while(myResults.next())
			{
				if(verbose)
				{
					System.out.println("Got a result");
				}
				attributes=new ConcurrentHashMap();
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
				DBObj tmp = new DBObj(attributes, false, tables);
				myReturn.add(tmp);
			}
		}
		catch(Exception e)
		{
			disconnect();
			return null;
		}
		disconnect();
		return myReturn;
	}
	
	public synchronized ArrayList getChallengeDefault(String challengeName)
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		ArrayList myReturn = new ArrayList();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM challenge_default INNER JOIN challenge_command_default ON challenge_default.challenge_name = challenge_command_default.challenge_name WHERE challenge_default.challenge_name = ? ORDER BY challenge_command_default.command_order ASC");
			myStmt.setString(1, challengeName);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			while(myResults.next())
			{
				if(verbose)
				{
					System.out.println("Got a result");
				}
				attributes=new ConcurrentHashMap();
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
				DBObj tmp = new DBObj(attributes, false, tables);
				myReturn.add(tmp);
			}
		}
		catch(Exception e)
		{
			disconnect();
			return null;
		}
		disconnect();
		return myReturn;
	}
	
	public synchronized ArrayList getChallengeEvaluation(String challengeName, String email)
	{
		if(verbose)
		{
			System.out.println("Connector signing in");
		}
		ArrayList tables=new ArrayList();
		tables.add("user");
		System.out.println(getConnection());
		if(verbose)
		{
			System.out.println("Connector got connection");
		}
		ConcurrentHashMap attributes=new ConcurrentHashMap();
		ArrayList myReturn = new ArrayList();
		try
		{
			PreparedStatement myStmt=connection.prepareStatement("SELECT * FROM challenge INNER JOIN challenge_evaluate_command ON challenge.challenge_name = challenge_evaluate_command.challenge_name INNER JOIN challenge_participant ON challenge.challenge_name = challenge_participant.challenge_name WHERE challenge.challenge_name = ? AND challenge_participant.email = ? ORDER BY challenge_evaluate_command.command_order ASC");
			myStmt.setString(1, challengeName);
			myStmt.setString(2, email);
			ResultSet myResults=myStmt.executeQuery();
			disconnect();
			ResultSetMetaData meta=myResults.getMetaData();
			int columns=meta.getColumnCount();
			while(myResults.next())
			{
				attributes=new ConcurrentHashMap();
				for(int x=1; x<=columns; x++)
				{
					if(meta.getColumnLabel(x).equals("password") || meta.getColumnLabel(x).equals("salt"))
					{
						
					}
					else
					{
						if(myResults.getObject(x)!=null)
						{
							attributes.put(meta.getColumnLabel(x), myResults.getObject(x));
						}
						else
						{
							attributes.put(meta.getColumnLabel(x), "");
						}
					}
				}
				DBObj tmp = new DBObj(attributes, false, tables);
				myReturn.add(tmp);
			}
		}
		catch(Exception e)
		{
			disconnect();
			return null;
		}
		disconnect();
		return myReturn;
	}
	
	public void assignChallenge(String challengeName, String email)
	{
		getConnection();
		try
		{
			PreparedStatement myStmt = connection.prepareStatement("INSERT INTO `challenge_participant`(`challenge_name`, `email`) VALUES (?,?)");
			myStmt.setString(1, challengeName);
			myStmt.setString(2, email);
			myStmt.execute();
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
		}
		disconnect();
	}
	
	public void unassignChallenge(String challengeName, String email)
	{
		getConnection();
		try
		{
			PreparedStatement myStmt = connection.prepareStatement("DELETE FROM `challenge_participant` WHERE `challenge_name`=? AND `email`=?");
			myStmt.setString(1, challengeName);
			myStmt.setString(2, email);
			myStmt.execute();
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
		}
		disconnect();
	}
	
	public boolean updateChallenge(String prevName, String newName, String openTime, String endTime, String description)
	{
		getConnection();
		try
		{
			PreparedStatement myStmt = connection.prepareStatement("UPDATE `challenge` SET `challenge_name`=?,`open_time`=?,`end_time`=?,`description`=? WHERE `challenge_name`=?");
			myStmt.setString(1, newName);
			myStmt.setString(2, openTime);
			myStmt.setString(3, endTime);
			myStmt.setString(4, description);
			myStmt.setString(5, prevName);
			myStmt.execute();
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
			return false;
		}
		disconnect();
		return true;
	}
	
	public boolean setGrade(String challenge, String email, String grade)
	{
		getConnection();
		try
		{
			PreparedStatement myStmt = connection.prepareStatement("UPDATE `challenge_participant` SET `grade`=? WHERE `challenge_name`=? AND `email`=?");
			myStmt.setString(1, grade);
			myStmt.setString(2, challenge);
			myStmt.setString(3, email);
			myStmt.execute();
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
			return false;
		}
		disconnect();
		return true;
	}
	
	public boolean createChallenge(String newName, String openTime, String endTime, String description, String email)
	{
		getConnection();
		try
		{
			PreparedStatement myStmt = connection.prepareStatement("INSERT INTO `challenge`(`challenge_name`, `open_time`, `end_time`, `description`, `admin_email`) VALUES (?,?,?,?,?)");
			myStmt.setString(1, newName);
			myStmt.setString(2, openTime);
			myStmt.setString(3, endTime);
			myStmt.setString(4, description);
			myStmt.setString(5, email);
			
			myStmt.execute();
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
			return false;
		}
		disconnect();
		assignChallenge(newName, email);
		return true;
	}
	
	public void deleteCommands(String prevChallengeName)
	{
		getConnection();
		try
		{
			PreparedStatement myStmt = connection.prepareStatement("DELETE FROM `challenge_command` WHERE `challenge_name` = ?");
			myStmt.setString(1, prevChallengeName);
			myStmt.execute();
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
		}
		disconnect();
	}
	
	public void deleteChallenge(String prevChallengeName, String email)
	{
		getConnection();
		try
		{
			PreparedStatement myStmt = connection.prepareStatement("DELETE FROM `challenge` WHERE `challenge_name` = ? AND `admin_email` = ?");
			myStmt.setString(1, prevChallengeName);
			myStmt.setString(2, email);
			
			myStmt.execute();
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
		}
		disconnect();
	}
	
	public void addCommand(String commandOrder, String commandName, String command, String challengeName)
	{
		getConnection();
		try
		{
			PreparedStatement myStmt = connection.prepareStatement("INSERT INTO `challenge_command`(`command_order`, `commandName`, `command`, `challenge_name`) VALUES (?, ?, ?, ?)");
			myStmt.setString(1, commandOrder);
			myStmt.setString(2, commandName);
			myStmt.setString(3, command);
			myStmt.setString(4, challengeName);
			myStmt.execute();
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
		}
		disconnect();
	}
	
	
	public void deleteUser(String email)
	{
		getConnection();
		try
		{
			PreparedStatement myStmt = connection.prepareStatement("DELETE FROM `user` WHERE `email`=?");
			myStmt.setString(1, email);
			myStmt.execute();
		}
		catch(Exception e)
		{
			disconnect();
			e.printStackTrace();
		}
		disconnect();
	}
	
	/**
	 * 
	 * @param toUpdate
	 */
	public synchronized User updateUser(User toUpdate, User fromUpdate, String fromIP, String fromRequest)
	{
		ConcurrentHashMap updateMap=toUpdate.getAttributeChanges();
		if(toUpdate.getWritten() || updateMap==null || updateMap.size()==0)
		{
			return getUser((String)toUpdate.getAttribute("email"));
		}
		ConcurrentHashMap oldMap=toUpdate.getAttributes();
		if(verbose)
		{
			System.out.println("Getting column names");
		}
		getConnection();
		ArrayList validColumns=getColumnNames("user");
		Iterator myIter=updateMap.keySet().iterator();
		String stmt="UPDATE user SET ";
		boolean first=true;
		ArrayList parameters=new ArrayList();
		while(myIter.hasNext())
		{
			Object key=myIter.next();
			Object tmp=updateMap.get(key);
			if(validColumns.contains(key))
			{
				if(!(tmp instanceof ArrayList))
				{
					if(first)
					{
						if(key.equals("password"))
						{
							parameters.add(tmp);
							parameters.add(updateMap.get("salt"));
							stmt+=key+" = SHA2(CONCAT(?, ?),512), ";
						}
						else
						{
							parameters.add(tmp);
							stmt+=key+" = ?, ";
						}
					}
					else
					{
						if(key.equals("password"))
						{
							parameters.add(tmp);
							parameters.add(updateMap.get("salt"));
							stmt+=key+" = SHA2(CONCAT(?, ?),512), ";
						}
						else
						{
							parameters.add(tmp);
							stmt+=key+" = ?, ";
						}
					}
				}
				else
				{
					
				}
			}
			else
			{
				if(tmp instanceof ArrayList)
				{
					
				}
			}
		}
		stmt+="changeIP=?, changeURL=?, changeUserEmail=?";
		parameters.add(fromIP);
		parameters.add(fromRequest);
		parameters.add(fromUpdate.getAttribute("email"));
		/*
		stmt+=", changeuseremail=?, changeuserrole=?, changeuserinstitution=?";
		parameters.add(fromUpdate.getAttribute("email"));
		parameters.add(fromUpdate.getCurRole().getAttribute("role"));
		parameters.add(fromUpdate.getCurRole().getAttribute("institution"));
		*/
		stmt+=" WHERE email=?";
		parameters.add(oldMap.get("email"));
		/*
		Role tmp=fromUpdate.getCurRole();
		ArrayList permissions=(ArrayList)tmp.getAttribute("permissions");
		boolean updatepermission=false;
		first=true;
		for(int x=0; x<permissions.size(); x++)
		{
			Permission perm=(Permission)permissions.get(x);
			if(perm.getAttribute("action").equals("update user"))
			{
				String addToStmt=((String)perm.getAttribute("targetrelation"));
				Scanner myScanner=new Scanner(addToStmt);
				String next="";
				String addToStmtFinal="";
				while(myScanner.hasNext())
				{
					next=myScanner.next();
					addToStmtFinal+=" ";
					if(next.equals("actionemail"))
					{
						parameters.add((String)fromUpdate.getAttribute("email"));
						addToStmtFinal+="?";
					}
					else if(next.equals("actionrole"))
					{
						parameters.add((String)(((Role)fromUpdate.getCurRole()).getAttribute("role")));
						addToStmtFinal+="?";
					}
					else if(next.equals("actionsuperroles"))
					{
						ArrayList ownerList=getSuperRoleNames((String)(((Role)fromUpdate.getCurRole()).getAttribute("role")));
						for(int y=0; y<ownerList.size(); y++)
						{
							parameters.add(ownerList.get(y));
							if(y==0)
							{
								addToStmtFinal+="?";
							}
							else
							{
								addToStmtFinal+=", ?";
							}
						}
					}
					else if(next.equals("actioninstitution"))
					{
						parameters.add((String)(((Role)fromUpdate.getCurRole()).getAttribute("institution")));
						addToStmtFinal+="?";
					}
					else if(next.equals("actionownerinstitutions"))
					{
						ArrayList ownerList=getOwnerInstitutionNames((String)(((Role)fromUpdate.getCurRole()).getAttribute("institution")));
						for(int y=0; y<ownerList.size(); y++)
						{
							parameters.add(ownerList.get(y));
							if(y==0)
							{
								addToStmtFinal+="?";
							}
							else
							{
								addToStmtFinal+=", ?";
							}
						}
					}
					else if(next.equals("targetemail"))
					{
						parameters.add((String)toUpdate.getAttribute("email"));
						addToStmtFinal+="?";
					}
					else if(next.equals("targetrole"))
					{
						parameters.add((String)(((Role)toUpdate.getCurRole()).getAttribute("role")));
						addToStmtFinal+="?";
					}
					else if(next.equals("targetsuperroles"))
					{
						ArrayList ownerList=getSuperRoleNames((String)(((Role)toUpdate.getCurRole()).getAttribute("role")));
						for(int y=0; y<ownerList.size(); y++)
						{
							parameters.add(ownerList.get(y));
							if(y==0)
							{
								addToStmtFinal+="?";
							}
							else
							{
								addToStmtFinal+=", ?";
							}
						}
					}
					else if(next.equals("targetinstitution"))
					{
						parameters.add((String)(((Role)toUpdate.getCurRole()).getAttribute("institution")));
						addToStmtFinal+="?";
					}
					else if(next.equals("targetownerinstitutions"))
					{
						ArrayList ownerList=getOwnerInstitutionNames((String)(((Role)toUpdate.getCurRole()).getAttribute("institution")));
						for(int y=0; y<ownerList.size(); y++)
						{
							parameters.add(ownerList.get(y));
							if(y==0)
							{
								addToStmtFinal+="?";
							}
							else
							{
								addToStmtFinal+=", ?";
							}
						}
					}
					else
					{
						addToStmtFinal+=next;
					}
				}
				if(first)
				{
					stmt+="("+addToStmtFinal+")";
					updatepermission=true;
					first=false;
				}
				else
				{
					stmt+=" OR ("+addToStmtFinal+")";
				}
			}
		}
		if(!updatepermission)
		{
			disconnect();
			return null;
		}
		*/
		try
		{
			getConnection();
			PreparedStatement myStmt=connection.prepareStatement(stmt);
			for(int x=0; x<parameters.size(); x++)
			{
				myStmt.setObject(x+1, parameters.get(x));
			}
			myStmt.executeUpdate();
			myStmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			disconnect();
			return null;
		}
		toUpdate.setWritten(true);
		disconnect();
		return getUser((String)toUpdate.getAttribute("email"));
	}
	
	/**
	 * 
	 * @param toUpdate
	 */
	public synchronized void challengeParticipantCodeWritten(String challengeName, String email, byte[] originalFile, byte[] obfuscatedFile)
	{
		String stmt="UPDATE `challenge_participant` SET `code_generated` = '1', `originalFile` = ?, `obfuscatedFile` = ? WHERE `challenge_participant`.`challenge_name` = ? AND `challenge_participant`.`email` = ?";
		try
		{
			getConnection();
			PreparedStatement myStmt=connection.prepareStatement(stmt);
			myStmt.setBytes(1, originalFile);
			myStmt.setBytes(2, obfuscatedFile);
			myStmt.setString(3, challengeName);
			myStmt.setString(4, email);
			myStmt.executeUpdate();
			myStmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			disconnect();
			return;
		}
		disconnect();
	}
	
	/**
	 * 
	 * @param toUpdate
	 */
	public synchronized void challengeParticipantCodeSubmitted(String challengeName, String email, byte[] writeFile, byte[] codeFile)
	{
		String stmt="UPDATE `challenge_participant` SET `submittedFile` = ?, `submittedWrittenFile` = ?, `submissionTime` = CURRENT_TIME() WHERE `challenge_participant`.`challenge_name` = ? AND `challenge_participant`.`email` = ?";
		try
		{
			getConnection();
			PreparedStatement myStmt=connection.prepareStatement(stmt);
			myStmt.setBytes(1, codeFile);
			myStmt.setBytes(2, writeFile);
			myStmt.setString(3, challengeName);
			myStmt.setString(4, email);
			myStmt.executeUpdate();
			myStmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			disconnect();
			return;
		}
		disconnect();
	}
	
	public synchronized void writeUserRequest(String userName, String email, String fname, String mname, String lname, String pass, String message, String type)
	{
		String salt = UUID.randomUUID().toString();
		
		String stmt="INSERT INTO `user_request` (`username`, `email`, `password`, `fName`, `mName`, `lName`, `lastLogon`, `currentVisit`, `previousVisit`, `loginIP`, `salt`, `changeIP`, `changeURL`, `changeUserEmail`, `displayRealName`, `passwordPlaintext`, `message`, `role`) VALUES (?, ?, SHA2(CONCAT(?, ?), 512), ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '', ?, '', '', '', '0', ?, ?, ?)";
		//String stmt2="INSERT INTO `role` (`email`, `role`) VALUES (?, ?)";
		
		try
		{
			getConnection();
			PreparedStatement myStmt=connection.prepareStatement(stmt);
			myStmt.setString(1, userName);
			myStmt.setString(2, email);
			myStmt.setString(3, pass);
			myStmt.setString(4, salt);
			myStmt.setString(5, fname);
			myStmt.setString(6, mname);
			myStmt.setString(7, lname);
			myStmt.setString(8, salt);
			myStmt.setString(9, pass);
			myStmt.setString(10, message);
			myStmt.setString(11, type);
			
			myStmt.executeUpdate();
			myStmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			disconnect();
			//return;
		}
		disconnect();
	}
	
	public synchronized void writeCsvUsers(UserCsvParser myParser)
	{
		for(int x=0; x<myParser.emailList.size(); x++)
		{
			HashMap tmp=(HashMap) myParser.userMap.get(myParser.emailList.get(x));
			//System.out.println(tmp.get("fname")+", "+tmp.get("lname")+", "+tmp.get("email")+", "+tmp.get("password"));
			String stmt="INSERT INTO `user` (`username`, `email`, `password`, `fName`, `mName`, `lName`, `lastLogon`, `currentVisit`, `previousVisit`, `loginIP`, `salt`, `changeIP`, `changeURL`, `changeUserEmail`, `displayRealName`, `passwordPlaintext`) VALUES (?, ?, SHA2(CONCAT(?, ?), 512), ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '', ?, '', '', '', '0', ?)";
			String stmt2="INSERT INTO `role` (`email`, `role`) VALUES (?, 'student')";
			String stmt3="INSERT INTO `challenge_participant` (`email`, `challenge_name`) VALUES (?, 'Assignment 1 A')";
			String stmt4="INSERT INTO `challenge_participant` (`email`, `challenge_name`) VALUES (?, 'Assignment 1 B')";
			try
			{
				getConnection();
				PreparedStatement myStmt=connection.prepareStatement(stmt);
				myStmt.setString(1, (String) tmp.get("email"));
				myStmt.setString(2, (String) tmp.get("email"));
				myStmt.setString(3, tmp.get("password").toString());
				myStmt.setString(4, tmp.get("salt").toString());
				myStmt.setString(5, (String) tmp.get("fname"));
				myStmt.setString(6, "");
				myStmt.setString(7, (String) tmp.get("lname"));
				myStmt.setString(8, (String) tmp.get("salt").toString());
				myStmt.setString(9, (String) tmp.get("password").toString());
				
				PreparedStatement myStmt2=connection.prepareStatement(stmt2);
				myStmt2.setString(1, (String) tmp.get("email"));
				
				PreparedStatement myStmt3=connection.prepareStatement(stmt3);
				myStmt3.setString(1, (String) tmp.get("email"));
				
				PreparedStatement myStmt4=connection.prepareStatement(stmt4);
				myStmt4.setString(1, (String) tmp.get("email"));
				
				myStmt.executeUpdate();
				myStmt.close();
				
				myStmt2.executeUpdate();
				myStmt2.close();
				
				myStmt3.executeUpdate();
				myStmt3.close();
				
				myStmt4.executeUpdate();
				myStmt4.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				disconnect();
				//return;
			}
			disconnect();
		}
	}
	
	public synchronized void writeCsvUsers(UserCsvParser myParser, String adminEmail)
	{
		for(int x=0; x<myParser.emailList.size(); x++)
		{
			HashMap tmp=(HashMap) myParser.userMap.get(myParser.emailList.get(x));
			//System.out.println(tmp.get("fname")+", "+tmp.get("lname")+", "+tmp.get("email")+", "+tmp.get("password"));
			String stmt="INSERT INTO `user` (`username`, `email`, `password`, `fName`, `mName`, `lName`, `lastLogon`, `currentVisit`, `previousVisit`, `loginIP`, `salt`, `changeIP`, `changeURL`, `changeUserEmail`, `displayRealName`, `passwordPlaintext`) VALUES (?, ?, SHA2(CONCAT(?, ?), 512), ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '', ?, '', '', '', '0', ?)";
			String stmt2="INSERT INTO `role` (`email`, `role`) VALUES (?, 'student')";
			String stmt3="INSERT INTO `challenge_participant` (`email`, `challenge_name`) VALUES (?, 'Assignment 1 A')";
			String stmt4="INSERT INTO `challenge_participant` (`email`, `challenge_name`) VALUES (?, 'Assignment 1 B')";
			try
			{
				getConnection();
				PreparedStatement myStmt=connection.prepareStatement(stmt);
				myStmt.setString(1, (String) tmp.get("email"));
				myStmt.setString(2, (String) tmp.get("email"));
				myStmt.setString(3, tmp.get("password").toString());
				myStmt.setString(4, tmp.get("salt").toString());
				myStmt.setString(5, (String) tmp.get("fname"));
				myStmt.setString(6, "");
				myStmt.setString(7, (String) tmp.get("lname"));
				myStmt.setString(8, (String) tmp.get("salt").toString());
				myStmt.setString(9, (String) tmp.get("password").toString());
				
				PreparedStatement myStmt2=connection.prepareStatement(stmt2);
				myStmt2.setString(1, (String) tmp.get("email"));
				
				PreparedStatement myStmt3=connection.prepareStatement(stmt3);
				myStmt3.setString(1, (String) tmp.get("email"));
				
				PreparedStatement myStmt4=connection.prepareStatement(stmt4);
				myStmt4.setString(1, (String) tmp.get("email"));
				
				myStmt.executeUpdate();
				myStmt.close();
				
				myStmt2.executeUpdate();
				myStmt2.close();
				
				myStmt3.executeUpdate();
				myStmt3.close();
				
				myStmt4.executeUpdate();
				myStmt4.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				disconnect();
				//return;
			}
			disconnect();
		}
	}
	
	public synchronized void writeUser(String username, String email, String fname, String mname, String lname, String password, String role, String administrator, String courseName)
	{
		courseName = courseName.replaceAll(" ", "_");
		String salt = UUID.randomUUID().toString();
		//System.out.println(tmp.get("fname")+", "+tmp.get("lname")+", "+tmp.get("email")+", "+tmp.get("password"));
		String stmt="INSERT INTO `user` (`username`, `email`, `password`, `fName`, `mName`, `lName`, `lastLogon`, `currentVisit`, `previousVisit`, `loginIP`, `salt`, `changeIP`, `changeURL`, `changeUserEmail`, `displayRealName`, `passwordPlaintext`) VALUES (?, ?, SHA2(CONCAT(?, ?), 512), ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '', ?, '', '', '', '0', ?)";
		String stmt2="INSERT INTO `role` (`email`, `role`, `administrator`, `course`) VALUES (?, ?, ?, ?)";
		try
		{
			getConnection();
			PreparedStatement myStmt=connection.prepareStatement(stmt);
			myStmt.setString(1, username);
			myStmt.setString(2, email);
			myStmt.setString(3, password);
			myStmt.setString(4, salt);
			myStmt.setString(5, fname);
			myStmt.setString(6, mname);
			myStmt.setString(7, lname);
			myStmt.setString(8, salt);
			myStmt.setString(9, password);
			
			PreparedStatement myStmt2=connection.prepareStatement(stmt2);
			myStmt2.setString(1, email);
			myStmt2.setString(2, role);
			myStmt2.setString(3, administrator);
			myStmt2.setString(4, courseName);
			
			myStmt.executeUpdate();
			myStmt.close();
			
			myStmt2.executeUpdate();
			myStmt2.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			disconnect();
			//return;
		}
		disconnect();
	}
}
