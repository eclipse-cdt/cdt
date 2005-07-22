/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin;

import java.net.URL;

/**
 * Helper class to launch a test
 */
public class TestPluginLauncher {
	
	public static final String APP_NAME= "org.eclipse.jdt.ui.tests.app";
	
	public static void run(String location, Class testCase, String[] args) {
		run(APP_NAME, location, testCase, args);
	}
	
	public static void run(String application, String location, Class testCase, String[] args) {
		try {
			String bootLocation= getBootLocation();
			int nArgs= args.length;
			String[] newArgs= new String[4 + nArgs];
			newArgs[0]= testCase.getName();
			for (int i= 0; i < nArgs; i++) {
				newArgs[1 + i]= args[i];
			}
			newArgs[1 + nArgs]= "-dev";
			newArgs[1 + nArgs + 1]= "bin";
			newArgs[1 + nArgs + 2]= "-debug";
			NewMain newMain= new NewMain(application, location, null, bootLocation, false);
			newMain.run(newArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getLocationFromProperties(String key) {
		return NewMain.getLocationFromProperties(key);
	}
	
	public static String getLocationFromProperties() {
		return NewMain.getLocationFromProperties("tests");
	}
	
	public static String getBootLocation() {
		URL url= TestPluginLauncher.class.getResource("TestPluginLauncher.class");
		String s= url.toString();
		int index= s.indexOf("/org.eclipse.jdt.ui.tests");
		if (index == -1)
			throw new IllegalArgumentException();
		s= s.substring(0, index);
		s= s + "/org.eclipse.core.boot/boot.jar";
		return s;
	}
}