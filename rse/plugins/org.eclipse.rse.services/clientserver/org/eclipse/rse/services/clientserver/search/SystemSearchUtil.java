/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.clientserver.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


/**
 * A utility class useful for search. It is a singleton.
 */
public class SystemSearchUtil {
	
	private static SystemSearchUtil instance;
	
	public static String FILE_NAMES_SEP_STRING = ",";

	/**
	 * Constructor for the utility class.
	 */
	private SystemSearchUtil() {
		super();
	}
	
	/**
	 * Gets the singleton instance of the class.
	 * @return the singleton instance.
	 */
	public static SystemSearchUtil getInstance() {
		
		if (instance == null) {
			instance = new SystemSearchUtil();
		}
		
		return instance;
	}
	
	/**
	 * Check whether a regex string is valid.
	 * @param regexString the regex string.
	 * @return <code>true</code> if the regex string is valid, <code>false</code> otherwise.
	 */
	public boolean isValidRegex(String regexString) {

		
		try {
			Pattern.compile(regexString);
			return true;
		}
		// if we get an exception, then pattern is not valid
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Returns the file names from the given file names string.
	 * @param fileNamesString the file names string.
	 * @return a set of file names, or an empty set if none.
	 * @throws NullPointerException if the file names string is <code>null</code>.
	 */
	public Set typesStringToSet(String fileNamesString) {
		
		if (fileNamesString == null) {
			throw new NullPointerException();
		}
		
		if (fileNamesString.equals("")) {
			return new HashSet();
		}
		
		Set result = new HashSet();
			
		// create a string tokenizer which has ',' as its delimiter
		StringTokenizer tokenizer = new StringTokenizer(fileNamesString, FILE_NAMES_SEP_STRING);

		// go through each token
		while (tokenizer.hasMoreTokens()) {
				
			// get rid of whitespace
			String currentExtension = tokenizer.nextToken().trim();
			result.add(currentExtension);
		}
			
		return result;		
	}
	
	/**
	 * Returns the file names from the given file names string.
	 * @param fileNamesString the file names string.
	 * @return a list of file names, or an empty list if none.
	 * @throws NullPointerException if the file names string is <code>null</code>.
	 */
	public List typesStringToList(String fileNamesString) {
		
		if (fileNamesString == null) {
			throw new NullPointerException();
		}
		
		if (fileNamesString.equals("")) {
			return new ArrayList();
		}
		
		List result = new ArrayList();
			
		// create a string tokenizer which has ',' as its delimiter
		StringTokenizer tokenizer = new StringTokenizer(fileNamesString, FILE_NAMES_SEP_STRING);

		// go through each token
		while (tokenizer.hasMoreTokens()) {
				
			// get rid of whitespace
			String currentExtension = tokenizer.nextToken().trim();
			result.add(currentExtension);
		}
			
		return result;		
	}
	
	/**
	 * Convert list of types to string.
	 * @param types the list of types.
	 * @return the string representing the list of types.
	 */
	public String typesListToString(List types) {
			
		// get the iterator for the list of types
		Iterator iter = types.iterator();
			
		return typesIterToString(iter);
	}
	
	/**
	 * Convert set of types to string.
	 * @param types the set of types.
	 * @return the string representing the set of types.
	 */
	public String typesSetToString(Set types) {
			
		// get the iterator for the set of types
		Iterator iter = types.iterator();
		
		return typesIterToString(iter);
	}
	
	/**
	 * Convert iterator of types to string.
	 * @param iter iterator for types.
	 * @return the string representing the set of types.
	 */
	public String typesIterToString(Iterator iter) {
		StringBuffer result = new StringBuffer();
			
		boolean first = true;
			
		// iterate over the types
		while (iter.hasNext()) {
				
			// if it's not the first entry, then precede with ',' followed by space, i.e. ' '
			if (!first) {
				result.append(FILE_NAMES_SEP_STRING);
				result.append(" ");
			}
			// if first entry, don't do anything
			else {
				first = false;
			}
				
			result.append(iter.next());
		}
			
		return result.toString();
	}
	
	
}