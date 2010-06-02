/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.testplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class DiffUtil {
	private static final String DIFF_CMD = "diff -ub";
	private static DiffUtil fInstance;
	
	private DiffUtil(){
		
	}
	
	public static DiffUtil getInstance(){
		if(fInstance == null)
			fInstance = new DiffUtil();
		return fInstance;
	}
	
	private static String createCommand(String location1, String location2){
		StringBuffer buf = new StringBuffer();
		buf.append(DIFF_CMD).append(" '").append(location1).append("' '").append(location2).append("'");
		return buf.toString(); 
	}
	
	public String diff(String location1, String location2){
		InputStream in = invokeDiff(location1, location2);
		if(in == null)
			return null;
		
		String charSet = null;
		BufferedReader br;
		if (null == charSet) { 
			br = new BufferedReader(new InputStreamReader(in));
		} else {
			try {
				br = new BufferedReader(new InputStreamReader(in, charSet));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		String line;
		String prev_key = null; // previous key read and saved in envVars
		String prev_value = null; // value of previous key
		StringBuffer buf = new StringBuffer();
		try {
			while ((line = br.readLine()) != null) {
				buf.append("\n");
				buf.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return buf.toString();
	}
	
	private InputStream invokeDiff(String location1, String location2){
		try {
			Process p = Runtime.getRuntime().exec(createCommand(location1, location2));
			return p.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
