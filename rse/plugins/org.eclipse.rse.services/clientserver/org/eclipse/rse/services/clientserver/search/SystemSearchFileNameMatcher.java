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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;


/**
 * A file name matcher for search.
 */
public class SystemSearchFileNameMatcher implements ISystemSearchMatcher {
	
	private String fileNamesString;
	private boolean isRegex;
	
	// for non regular expression
	private List nonRegexMatchers;
	
	// for regular regular expression
	private Pattern pattern;

	/**
	 * Constructor to create a file name matcher.
	 * @param fileNamesString the file names string.
	 * @param isCaseSensitive <code>true</code> if the matching should be done in a case sensitive manner, <code>false</code> otherwise.
	 * @param isRegex <code>true</code> if the file names string is a regular expression, <code>false</code> otherwise.
	 * @throws NullPointerException if the file names string is <code>null</code>.
	 */
	public SystemSearchFileNameMatcher(String fileNamesString, boolean isCaseSensitive, boolean isRegex) {
		
		if (fileNamesString == null) {
			throw new NullPointerException();
		}
		
		this.fileNamesString = fileNamesString;
		this.isRegex = isRegex;
		
		// if the file names string is a regular expression, then simply add it
		if (isRegex) {

			
			try {
				if (isCaseSensitive) {
					pattern = Pattern.compile(fileNamesString);
				}
				else {
					pattern = Pattern.compile(fileNamesString, Pattern.CASE_INSENSITIVE);
				}
			}
			catch (Exception e) {
				// TODO: log it. How?
			}
		}
		// otherwise, get all the names from the names string, and add a non regex matcher for each
		else {
			nonRegexMatchers = new Vector();
			
			Set names = SystemSearchUtil.getInstance().typesStringToSet(fileNamesString);
			
			Iterator iter = names.iterator();
		
			while (iter.hasNext()) {
				String name = (String)(iter.next());
				SystemNonRegexMatcher nonRegexMatcher = new SystemNonRegexMatcher(name, !isCaseSensitive, false);
				nonRegexMatchers.add(nonRegexMatcher);
			}
		}
	}
	
	/**
	 * Returns whether the file names string is empty.
	 * @return <code>true</code> if the file names string is empty, <code>false</code> otherwise.
	 */
	public boolean isFileNamesStringEmpty() {
		return fileNamesString.length() == 0;
	}
	
	/**
	 * Returns whether the file names string is "*".
	 * @return <code>true</code> if the file names string is "*", <code>false</code> otherwise.
	 */
	public boolean isFileNamesStringAsterisk() {
		return fileNamesString.equals("*");
	}

	/**
	 * Returns whether there is a match for the given input. Returns <code>true</code> if the file names string
	 * is an empty string.
	 * @see org.eclipse.rse.services.clientserver.ISystemSearchMatcher#matches(java.lang.String)
	 */
	public boolean matches(String input) {
		
		if (isFileNamesStringEmpty()) {
			return true;
		}
		
		if (isRegex) {
			return pattern.matcher(input).find();
		}
		else {
			Iterator iter = nonRegexMatchers.iterator();
			
			while (iter.hasNext()) {
				SystemNonRegexMatcher matcher = (SystemNonRegexMatcher)iter.next();
				
				// TODO (KM): in SystemSearchStringMatcher, we had to use find() instead of match().
				// Should we use this here as well? What is the difference between match() and find()? Investigate.
				if (matcher.match(input)) {
					return true;
				}
			}
			
			return false;
		}
	}
}