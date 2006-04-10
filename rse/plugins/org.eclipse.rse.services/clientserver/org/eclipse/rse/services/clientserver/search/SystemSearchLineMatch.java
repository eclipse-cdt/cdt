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
import java.util.Iterator;
import java.util.List;

/**
 * This class holds information about search results for a line of text. Its instance variables
 * are public so that we can access them without having to call methods (for performance reasons).
 */
public class SystemSearchLineMatch {
	
	private String line;
	private int lineNumber;
	private List matches;

	/**
	 * Constructor to create a search result for a line.
	 * @param line the line.
	 * @param lineNumber the line number.
	 */
	public SystemSearchLineMatch(String line, int lineNumber) {
		this.line = line;
		this.lineNumber = lineNumber;
		this.matches = new ArrayList();
	}
	
	/**
	 * Returns the line that matched.
	 * @return the line.
	 */
	public String getLine() {
		return line;
	}
	
	/**
	 * Returns the line number of the line that matched.
	 * @return the line number.
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Gets the iterator for the matches in the line.
	 * @return the iterator over the list of matches.
	 */
	public Iterator getMatches() {
		return matches.iterator();
	}
	
	/**
	 * Gets the number of matches in the line.
	 * @return the number of matches.
	 */
	public int getNumOfMatches() {
		return matches.size();
	}
	
	/**
	 * Adds an entry.
	 * @param startOffset the offset from the beginning of the file where the match begins.
	 * @param endOffset the offset from the beginning of the file where the match ends.
	 */
	public void addEntry(int startOffset, int endOffset) {
		matches.add(new SystemSearchMatch(startOffset, endOffset));
	}
}