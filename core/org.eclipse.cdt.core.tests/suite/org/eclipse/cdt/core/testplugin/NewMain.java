/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.core.testplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/** 
 * Application is responsible for calling core launch api
 */

public class NewMain extends Main {
	private static final String DEFAULT_APPLICATION= "org.eclipse.ui.workbench";
	
	
	public NewMain(String application, String location, URL pluginPathLocation, String bootLocation, boolean debug) throws IOException {
		this.application= application;
		this.location= location;
		this.pluginPathLocation= pluginPathLocation;
		this.bootLocation= bootLocation;
	}
	
	public static void main(String[] args) {
		try {
			String location= getLocationFromProperties("platform");
			new NewMain(DEFAULT_APPLICATION, location, null, null, true).run(args);
		} catch (Throwable e) {
			System.out.println("Exception launching the Eclipse Platform UI:");
			e.printStackTrace();
		}
		System.exit(0);
	}
	

	/**
	 * Run this launcher with the arguments specified in the given string.
	 * This is a short cut method for people running the launcher from
	 * a scrapbook (i.e., swip-and-doit facility).
	 */
	public static void main(String argString) throws Exception {
		Vector list= new Vector(5);
		for (StringTokenizer tokens= new StringTokenizer(argString, " "); tokens.hasMoreElements();)
			list.addElement((String) tokens.nextElement());
		main((String[]) list.toArray(new String[list.size()]));
	}
	
	public static String getLocationFromProperties(String key) {
		Properties properties= new Properties();
		try {
			FileInputStream fis= new FileInputStream(getSettingsFile());
			properties.load(fis);
			return properties.getProperty(key);
		} catch (IOException e) {
		}
		return null;
	}	
	
	private static File getSettingsFile() {
		String home= System.getProperty("user.home");
		if (home == null) {
			System.out.println("Home dir not defined");
			return null;
		}
		return new File(home, "eclipse-workspaces.properties");	
	}	
}