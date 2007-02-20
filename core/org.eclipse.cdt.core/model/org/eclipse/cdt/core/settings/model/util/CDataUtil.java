/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

public class CDataUtil {
	private static Random randomNumber;
	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static int genRandomNumber(){
		if (randomNumber == null) {
			// Set the random number seed
			randomNumber = new Random();
			randomNumber.setSeed(System.currentTimeMillis());
		}
		int i = randomNumber.nextInt();
		if (i < 0) {
			i *= -1;
		}
		return i;
	}
	
	public static String genId(String baseId){
		String suffix = new Integer(genRandomNumber()).toString();
		return baseId != null ? 
				new StringBuffer(baseId).append(".").append(suffix).toString() 	//$NON-NLS-1$
				: suffix;
	}
	
	public static boolean objectsEqual(Object o1, Object o2){
		if(o1 == null)
			return o2 == null;
		return o1.equals(o2);
	}
	
	public static String arrayToString(String[] array, String separator){
		if(array == null)
			return null;
		if(array.length == 0)
			return "";
		if(array.length == 1)
			return array[0];
		StringBuffer buf = new StringBuffer();
		buf.append(array[0]);
		for(int i = 1; i < array.length; i++){
			buf.append(separator).append(array[i]);
		}
		
		return buf.toString();
	}

	public static String[] stringToArray(String string, String separator){
		if(string == null)
			return null;
		if(string.length() == 0)
			return EMPTY_STRING_ARRAY;
		StringTokenizer t = new StringTokenizer(string, separator);
		List list = new ArrayList(t.countTokens());
		while (t.hasMoreElements()) {
			list.add(t.nextToken());
		}
		return (String[])list.toArray(new String[list.size()]);
	}

}
