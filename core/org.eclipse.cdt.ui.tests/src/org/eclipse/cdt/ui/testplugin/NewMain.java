/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.testplugin;

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
	private static final String DEFAULT_APPLICATION= "org.eclipse.ui.workbench"; //$NON-NLS-1$
	
	
	public NewMain(String application, String location, URL pluginPathLocation, String bootLocation, boolean debug) throws IOException {
		this.application= application;
		this.location= location;
		this.pluginPathLocation= pluginPathLocation;
		this.bootLocation= bootLocation;
	}
	
	public static void main(String[] args) {
		try {
			String location= getLocationFromProperties("platform"); //$NON-NLS-1$
			new NewMain(DEFAULT_APPLICATION, location, null, null, true).run(args);
		} catch (Throwable e) {
			System.out.println("Exception launching the Eclipse Platform UI:"); //$NON-NLS-1$
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
		for (StringTokenizer tokens= new StringTokenizer(argString, " "); tokens.hasMoreElements();) //$NON-NLS-1$
			list.addElement(tokens.nextElement());
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
		String home= System.getProperty("user.home"); //$NON-NLS-1$
		if (home == null) {
			System.out.println("Home dir not defined"); //$NON-NLS-1$
			return null;
		}
		return new File(home, "eclipse-workspaces.properties");	 //$NON-NLS-1$
	}	
}